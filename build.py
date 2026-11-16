"""Build the unsigned demo APK with the Android SDK, JDK 17+ and Python 3.11+."""

import argparse
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parent
BUILD_TOOLS = '36.1.0'


def run(*args):
    subprocess.run([str(value) for value in args], check=True)


def executable(directory, name):
    return directory / (name + ('.exe' if os.name == 'nt' else ''))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--sdk', required=True, type=Path, help='Android SDK directory')
    parser.add_argument('--jdk', required=True, type=Path, help='Trusted JDK installation directory')
    parser.add_argument('--out', type=Path, default=ROOT / 'build')
    parser.add_argument('--instrumentation', action='store_true', help='Also build the unsigned UI test APK')
    args = parser.parse_args()
    sdk = args.sdk.resolve()
    tools = sdk / 'build-tools' / BUILD_TOOLS
    jdk_bin = args.jdk.resolve() / 'bin'
    java, javac = (executable(jdk_bin, name) for name in ('java', 'javac'))
    if not java.is_file() or not javac.is_file():
        parser.error('--jdk must point to a JDK installation containing bin/java and bin/javac')

    def tool(name):
        return executable(tools, name)
    android = sdk / 'platforms' / 'android-36' / 'android.jar'
    output = args.out.resolve()
    output.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix='li-watch-ui-', dir=output) as scratch:
        temp = Path(scratch)
        resources = temp / 'compiled.zip'
        generated = temp / 'generated'
        generated.mkdir()
        assets = temp / 'assets'
        assets.mkdir()
        shutil.copyfile(ROOT / 'LICENSE', assets / 'LICENSE.txt')
        run(tool('aapt2'), 'compile', '--dir', ROOT / 'app/res', '-o', resources)
        base = temp / 'base.apk'
        run(tool('aapt2'), 'link', '-o', base, '-I', android, '--manifest',
            ROOT / 'app/AndroidManifest.xml', '--java', generated, '-A', assets, resources)

        def dex(name, sources, extra_classpath=None):
            classes = temp / (name + '-classes')
            classes.mkdir()
            cp = str(android) if extra_classpath is None else os.pathsep.join((str(android), str(extra_classpath)))
            run(javac, '-encoding', 'UTF-8', '--release', '8', '-Xlint:-options', '-cp', cp,
                '-d', classes, *sources)
            target = temp / (name + '-dex')
            target.mkdir()
            command = [java, '-cp', tools / 'lib/d8.jar', 'com.android.tools.r8.D8', '--release',
                       '--min-api', '28', '--lib', android, '--output', target]
            if extra_classpath is not None:
                command.extend(['--classpath', extra_classpath])
            run(*command, *sorted(classes.rglob('*.class')))
            return target / 'classes.dex', classes

        def finish(name, resource_apk, bytecode):
            unsigned = temp / (name + '-raw.apk')
            shutil.copyfile(resource_apk, unsigned)
            with zipfile.ZipFile(unsigned, 'a', compression=zipfile.ZIP_DEFLATED) as zipped:
                zipped.write(bytecode, 'classes.dex')
            run(tool('zipalign'), '-f', '4', unsigned, output / (name + '-unsigned.apk'))

        bytecode, classes = dex('app', sorted((ROOT / 'app/src').rglob('*.java')) + sorted(generated.rglob('*.java')))
        finish('li-watch-ui', base, bytecode)
        if args.instrumentation:
            test = temp / 'test.apk'
            run(tool('aapt2'), 'link', '-o', test, '-I', android, '--manifest', ROOT / 'tests/AndroidManifest.xml')
            test_dex, _ = dex('test', sorted((ROOT / 'tests/src').rglob('*.java')), classes)
            finish('li-watch-ui-test', test, test_dex)


if __name__ == '__main__':
    main()
