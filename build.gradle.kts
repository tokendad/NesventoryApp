// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
}

// Force patched versions of vulnerable transitive dependencies from build tooling (AGP/UTP).
// These libraries are pulled in by Gradle plugins and the instrumented test infrastructure —
// they are NOT included in the release APK.
subprojects {
    configurations.all {
        resolutionStrategy {
            force("io.netty:netty-codec-http:4.1.133.Final")
            force("io.netty:netty-codec-http2:4.1.133.Final")
            force("io.netty:netty-codec:4.1.133.Final")
            force("io.netty:netty-handler:4.1.133.Final")
            force("io.netty:netty-handler-proxy:4.1.133.Final")
            force("io.netty:netty-common:4.1.133.Final")
            force("io.netty:netty-buffer:4.1.133.Final")
            force("io.netty:netty-transport:4.1.133.Final")
            force("io.netty:netty-resolver:4.1.133.Final")
            force("io.netty:netty-transport-native-unix-common:4.1.133.Final")
            force("org.bouncycastle:bcprov-jdk18on:1.84")
            force("org.bouncycastle:bcpkix-jdk18on:1.84")
        }
    }
}