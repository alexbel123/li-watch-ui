# Security and privacy

Li Watch UI is an offline demo. It uses sample readings, has no account, does not request Android permissions and includes no network, Bluetooth, telemetry or vehicle integration. The only setting is the selected language (`ru` or `en`), saved locally by Android or in the app's private settings on Android 12 and older. App backup is disabled in the manifest.

The only exported Android component is the launcher activity; it renders the same screen regardless of the launch intent. Release APKs are signed separately. Signing keys and passwords are never included in this repository or in release archives. The SHA-256 list in the release identifies its exact downloadable files.

Please report security issues using GitHub's private vulnerability reporting for this repository. Describe the affected version and behavior. Do not attach credentials, VINs or real device logs.

Демо не подключается к автомобилю и не собирает пользовательские данные. На устройстве сохраняется только выбранный язык. О проблеме безопасности можно сообщить через приватную форму GitHub на вкладке **Security**.
