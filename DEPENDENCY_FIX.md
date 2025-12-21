# Gradle Dependency Compatibility Fix

## Issue Summary

The project was experiencing a build failure with the following error:

```
java.lang.NoClassDefFoundError: org/gradle/api/internal/HasConvention
```

## Root Cause

The error was caused by incompatible versions between Gradle, Android Gradle Plugin (AGP), and Kotlin Gradle Plugin:

1. **Gradle 9.0.0** - This version removed the internal `HasConvention` class
2. **Android Gradle Plugin 8.13.2** - This version doesn't exist in Maven repositories; requires Gradle 8.x, not 9.0
3. **Kotlin Gradle Plugin 1.9.20** - This version is incompatible with Gradle 9.0.0 (requires at least Kotlin 2.3.0 for Gradle 9.0 compatibility)

## Solution Applied

### Version Changes

| Component | Old Version | New Version | Reason |
|-----------|-------------|-------------|---------|
| Gradle | 9.0.0 | 8.9 | Gradle 9.0 removed `HasConvention` class; Gradle 8.9 is stable and compatible |
| Android Gradle Plugin | 8.13.2 | 8.5.2 | Version 8.13.2 doesn't exist; 8.5.2 is stable and well-tested |
| Kotlin Gradle Plugin | 1.9.20 | 1.9.20 | No change needed with Gradle 8.9 |

### Files Modified

1. **gradle/wrapper/gradle-wrapper.properties**
   - Changed distribution URL from `gradle-9.0.0-bin.zip` to `gradle-8.9-bin.zip`

2. **build.gradle.kts**
   - Updated AGP classpath dependency from `8.13.2` to `8.5.2`

3. **gradle/libs.versions.toml**
   - Updated AGP version reference from `8.13.2` to `8.5.2`

## Compatibility Matrix

The selected versions provide the following compatibility:

- **Gradle 8.9** is compatible with:
  - Android Gradle Plugin 8.5.x
  - Kotlin Gradle Plugin 1.9.x
  - JDK 17 (as configured in the project)

- **Android Gradle Plugin 8.5.2** requires:
  - Minimum Gradle version: 8.7
  - JDK 17 or higher

- **Kotlin Gradle Plugin 1.9.20** works with:
  - Gradle 7.6.3 through 8.9
  - Kotlin 1.9.x compiler version

## Verification Steps

To verify the fix works on your local machine:

```bash
# Clean previous build artifacts
./gradlew clean

# Test with a simple task
./gradlew help

# Build the project
./gradlew build
```

## Future Upgrade Path

If you want to upgrade to newer versions in the future:

### Option 1: Stay on Gradle 8.x (Recommended for stability)
- Gradle: 8.10 or 8.11
- Android Gradle Plugin: 8.7.x or later
- Kotlin: Can remain at 1.9.20 or upgrade to 2.0.x

### Option 2: Upgrade to Gradle 9.0 (Requires more changes)
- Gradle: 9.0.0 or later
- Android Gradle Plugin: Would need preview/beta version 9.x
- Kotlin Gradle Plugin: **Must upgrade to at least 2.3.0**
- This option is not recommended for production until all plugins stabilize

## References

- [Gradle Compatibility Matrix](https://docs.gradle.org/current/userguide/compatibility.html)
- [Android Gradle Plugin Release Notes](https://developer.android.com/build/releases/gradle-plugin)
- [Kotlin Gradle Plugin Compatibility](https://kotlinlang.org/docs/gradle-configure-project.html)
