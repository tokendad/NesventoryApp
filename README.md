# NesventoryNew

An Android application for managing your NesVentory with AI-powered item detection.

## Features

- **Inventory Management**: Keep track of your items, their locations, brands, and values.
- **AI Item Detection**: Use your camera or gallery to automatically identify items and populate details.
- **Location Tracking**: Organize your items by physical locations.
- **Play Store Integrated**: Automated deployment workflows for internal testing.

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer.
- JDK 17.
- Android SDK 36 (target).

### Building

To build the project, run:

```bash
./gradlew assembleDebug
```

To build the release App Bundle:

```bash
./gradlew bundleRelease
```

## Workflows

The project uses GitHub Actions for CI/CD:
- **Deploy to Play Store**: Automatically builds and uploads the app to the Internal Testing track on push to `main`.
- **Release Management**: (Coming soon) Automated versioning and release notes.

## License

This project is licensed under the terms of the MIT license.
