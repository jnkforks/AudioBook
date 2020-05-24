object GradlePluginVersion {
    const val GOOGLE_SERVICES = "4.3.3"
    const val ANDROID_GRADLE = "3.5.0"
    const val KTLINT_GRADLE = "8.2.0"
    const val DETEKT = "1.0.1"
    const val GRADLE_VERSION_PLUGIN = "0.22.0"
}

object GradlePluginId {
    const val DETEKT = "io.gitlab.arturbosch.detekt"
    const val KTLINT_GRADLE = "org.jlleitschuh.gradle.ktlint"
    const val ANDROID_APPLICATION = "com.android.application"
    const val ANDROID_DYNAMIC_FEATURE = "com.android.dynamic-feature"
    const val ANDROID_LIBRARY = "com.android.library"
    const val KOTLIN_KAPT="kotlin-kapt"
    const val KOTLIN_ANDROID = "org.jetbrains.kotlin.android"
    const val KOTLIN_ANDROID_EXTENSIONS = "kotlin-android-extensions"
    const val GRADLE_VERSION_PLUGIN = "com.github.ben-manes.versions"
    const val SAFE_ARGS = "androidx.navigation.safeargs"
    const val OSS_LICENSES = "com.google.android.gms.oss-licenses-plugin"
    const val GOOGLE_SERVICES ="com.google.gms.google-services"
}

object GradleDependency {
    const val KOTLIN = "org.jetbrains.kotlin:kotlin-gradle-plugin:${CoreVersion.KOTLIN}"
    const val ANDROID_GRADLE = "com.android.tools.build:gradle:${GradlePluginVersion.ANDROID_GRADLE}"
    const val KTLINT_GRADLE = "org.jlleitschuh.gradle:ktlint-gradle:${GradlePluginVersion.KTLINT_GRADLE}"
    const val SAFE_ARGS = "androidx.navigation:navigation-safe-args-gradle-plugin:${CoreVersion.NAVIGATION}"
    const val OSS_LICENSES = "com.google.android.gms:oss-licenses-plugin:${CoreVersion.OSS_LICENSES}"
    const val GOOGLE_SERVICES = "com.google.gms:google-services:${GradlePluginVersion.GOOGLE_SERVICES}"
}
