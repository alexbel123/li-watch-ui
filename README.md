# Li Watch UI

[English](#english) · [APK и исходники](https://github.com/alexbel123/li-watch-ui/releases/latest) · [Безопасность](SECURITY.md)

Демо интерфейса для Galaxy Watch 4 и других часов с Wear OS. На круглом экране два значения запаса хода остаются в одной строке, даже при крупном шрифте. В меню **Язык приложения** можно выбрать русский или английский.

На экране — вымышленные значения. Приложение работает офлайн, не запрашивает разрешений и не подключается к автомобилю. Это самостоятельная демонстрация интерфейса, без оригинального APK и компонентов Li Auto.

В разделе [Releases](https://github.com/alexbel123/li-watch-ui/releases/latest) скачайте `li-watch-ui-v1.0.0.apk`. Там же есть ZIP исходников и SHA-256. APK устанавливается как отдельное приложение **Li Watch UI**. Поддерживается Android 9 (API 28) и новее.

Для сборки нужны Python 3.11+, JDK 17+, Android SDK Platform 36 и Build Tools 36.1.0. Укажите пути к установленным SDK и JDK. Сценарий запускает инструменты из этих каталогов и не скачивает зависимости.

```sh
python build.py --sdk /path/to/android-sdk --jdk /path/to/jdk
```

Результат `build/li-watch-ui-unsigned.apk` нужно подписать `apksigner` своим ключом перед установкой. Флаг `--instrumentation` также собирает тестовый APK. Подпишите оба APK одним тестовым ключом, установите их на эмулятор и запустите:

```sh
adb shell am instrument -w dev.liwatch.ui.test/dev.liwatch.ui.test.RangeInstrumentation
```

[MIT](LICENSE) распространяется на весь код, ресурсы и иконки этого репозитория.

## English

An offline Wear OS dashboard demo for Galaxy Watch 4 and other watches. The electric and gasoline range values fit side by side on a round screen. Select **App language** to switch between English and Russian.

The dashboard uses fictional readings. It requests no permissions, does not connect to a vehicle, and contains no Li Auto APK or libraries. Download the APK, source ZIP and SHA-256 from [Releases](https://github.com/alexbel123/li-watch-ui/releases/latest). The demo installs separately as **Li Watch UI** and runs on Android 9 (API 28) or later.

Build with Python 3.11+, JDK 17+, Android SDK Platform 36 and Build Tools 36.1.0 as shown above. Sign the unsigned APK with your own key before installation. Use `--instrumentation` to build the layout and manifest tests.

[MIT license](LICENSE) · [Security and privacy](SECURITY.md)
