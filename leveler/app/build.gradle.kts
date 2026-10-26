plugins {
    id("com.android.application")
}

android {
    namespace = "com.adityak.leveler"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.adityak.leveler"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "0.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
