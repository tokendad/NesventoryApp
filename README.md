# Android-NesVentory

**Version: 0.1.0-alpha**

Android companion app for [NesVentory](https://github.com/tokendad/NesVentory) - a modern home inventory management system.

## 🚀 Features

### Core Functionality
- 📱 **Login with Server Settings** - Configure your NesVentory server connection
- 🔄 **Auto URL Switching** - Automatically switches between local and remote URLs based on WiFi SSID
- 📦 **Inventory View** - View and manage your inventory items
- 📍 **Locations** - Browse inventory locations
- 🔐 **Secure Storage** - Credentials stored securely using Android DataStore

### Server Configuration
The login screen provides options to configure:
- **API Access Token** - Optional API token for authentication
- **Remote URL** - Your NesVentory server URL (e.g., `https://nesventory.example.com`)
- **Local URL** - Local network URL (e.g., `http://192.168.1.100:8000`)
- **Local WiFi SSID** - WiFi network name that triggers automatic switch to local URL

### Smart URL Switching
When connected to the specified WiFi SSID, the app automatically uses the local URL for faster performance. When on other networks, it seamlessly switches to the remote URL.

## 📱 Screenshots

*Coming soon*

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3 Design
- **Architecture**: MVVM with Repository pattern
- **Dependency Injection**: Hilt
- **Networking**: Retrofit with Kotlin Serialization
- **Storage**: DataStore Preferences
- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 15+ (API 36)

## 📋 Requirements

- Android 8.0 (API level 26) or higher
- Access to a NesVentory server instance

## 🚀 Getting Started

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/tokendad/Android-NesVentory.git
   cd Android-NesVentory
   ```

2. Open the project in Android Studio (Hedgehog or newer)

3. Build the project:
   ```bash
   ./gradlew assembleDebug
   ```

4. Install on your device:
   ```bash
   ./gradlew installDebug
   ```

### Configuration

1. Launch the app
2. Tap "Server Settings" on the login screen
3. Configure your server connection:
   - Enter your NesVentory server URLs
   - Optionally set the local WiFi SSID for automatic switching
4. Save settings and login with your NesVentory credentials

## 📖 API Compatibility

This app is designed to work with [NesVentory v5.x](https://github.com/tokendad/NesVentory). It uses the following API endpoints:

- `POST /token` - OAuth2 token authentication
- `GET /users/me` - Current user information
- `GET /items/` - List all items
- `GET /locations/` - List all locations

## 🔧 Development

### Project Structure

```
app/
├── src/main/
│   ├── java/com/nesventory/android/
│   │   ├── data/
│   │   │   ├── api/           # API service definitions
│   │   │   ├── model/         # Data models
│   │   │   ├── preferences/   # DataStore preferences
│   │   │   └── repository/    # Data repositories
│   │   ├── di/                # Hilt dependency injection
│   │   ├── ui/
│   │   │   ├── dashboard/     # Dashboard screen
│   │   │   ├── login/         # Login screen with settings
│   │   │   └── theme/         # App theming
│   │   └── util/              # Utility classes
│   └── res/                   # Android resources
├── build.gradle.kts           # App-level build configuration
└── proguard-rules.pro         # ProGuard rules
```

### Commit Message Guidelines

This project uses [Conventional Commits](https://www.conventionalcommits.org/). Please use one of the following prefixes:

- `feat:` - A new feature
- `fix:` - A bug fix
- `docs:` - Documentation changes
- `style:` - Code style changes
- `refactor:` - Code refactoring
- `perf:` - Performance improvements
- `test:` - Test changes
- `build:` - Build system changes
- `ci:` - CI configuration changes
- `chore:` - Maintenance tasks

## 🚀 Google Play Deployment

This project includes a GitHub Actions workflow for deploying releases to Google Play Console.

### Required Secrets

To enable Google Play deployment, configure the following secrets in your GitHub repository:

| Secret | Description |
|--------|-------------|
| `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | Google Play service account JSON key with release permissions |
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded Android keystore file |
| `ANDROID_KEYSTORE_PASSWORD` | Password for the keystore |
| `ANDROID_KEY_ALIAS` | Key alias within the keystore |
| `ANDROID_KEY_PASSWORD` | Password for the key |

### Setting Up Google Play Service Account

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create or select a project
3. Enable the Google Play Android Developer API
4. Create a service account with JSON key
5. In [Google Play Console](https://play.google.com/console/), grant the service account "Release" permissions
6. Add the JSON key contents to `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` secret

### Deployment Workflow

The deployment can be triggered:
- **Manually**: Go to Actions → "Deploy to Google Play" → Run workflow
- **Automatically**: When a GitHub release is published

Available tracks: `internal`, `alpha`, `beta`, `production`

## 📄 License

This project is open source. See the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues and pull requests.

## 📞 Support

For issues and support:
- [Android App Issues](https://github.com/tokendad/Android-NesVentory/issues)
- [NesVentory Server Issues](https://github.com/tokendad/NesVentory/issues)
