# TakataGit 

TakataGit adalah aplikasi Android berbasis Jetpack Compose untuk melihat daftar log session dan detail log terminal secara real-time.

## Fitur

- Daftar log dengan search dan sort lokal
- Detail log bergaya terminal
- Polling otomatis untuk list dan detail
- Zoom terminal dengan pinch 2 jari
- Scroll horizontal dan vertical di terminal
- Pengaturan `endpoint` dan `API key`
- Signed release build
- APK split per ABI agar ukuran kecil

## Stack

- Kotlin
- Jetpack Compose
- Retrofit 2
- OkHttp 3/4
- ViewModel
- StateFlow

## Konfigurasi App

Default app settings:

- Endpoint: `takatagit.dawg.web.id`
- API key: `akucintasagiri`

Endpoint bisa diisi dengan:

- domain saja, contoh: `takatagit.dawg.web.id`
- full URL, contoh: `https://takatagit.dawg.web.id/api/v1/`

## Requirement Lokal

- Java 21 untuk Gradle runtime
- Android SDK:
  - `platform-tools`
  - `platforms;android-35`
  - `build-tools;35.0.0`

## Build Lokal

Buat `local.properties`:

```properties
sdk.dir=/path/to/android-sdk
```

Build release:

```bash
JAVA_HOME=/path/to/java-21 ./gradlew --no-daemon assembleRelease
```

Output APK:

- `app/build/outputs/apk/release/app-arm64-v8a-release.apk`
- `app/build/outputs/apk/release/app-armeabi-v7a-release.apk`

## APK yang Dipasang

Untuk mayoritas HP Android modern:

- gunakan `app-arm64-v8a-release.apk`

Untuk device 32-bit lama:

- gunakan `app-armeabi-v7a-release.apk`

## Signing Lokal

Project ini membaca signing config dari `keystore.properties`.

Format:

```properties
storeFile=signing/harutaka-release.jks
storePassword=your-password
keyAlias=your-alias
keyPassword=your-password
```

File berikut di-ignore dan tidak boleh di-commit:

- `keystore.properties`
- `signing/`
- `local.properties`

## GitHub Actions

Workflow tersedia di:

- [.github/workflows/android-build.yml](/workspaces/app-takatagit/.github/workflows/android-build.yml)

Workflow akan:

- setup Java 21
- setup Android SDK
- build `assembleRelease`
- upload APK sebagai artifact
- upload mapping/logs bila tersedia

Artifact:

- `takatagit-release-apk`
- `takatagit-build-reports`

## Secrets GitHub Actions

Jika ingin release build signed di GitHub Actions, tambahkan secrets berikut:

- `ANDROID_KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

Generate base64 keystore:

```bash
base64 -w 0 signing/harutaka-release.jks
```

## Launcher Icon

Launcher icon diambil dari:

- [takatagit.ico](/workspaces/app-takatagit/takatagit.ico)

Icon Android telah dikonversi menjadi resource launcher PNG + adaptive icon.

## Catatan UI Terminal

- Log terbaru berada di bagian paling bawah
- Timeline, level, dan message dipisah seperti code line viewer
- Initial loading memakai skeleton
- Manual refresh memakai spinner besar
- Auto refresh memakai indikator kecil/progress tipis
