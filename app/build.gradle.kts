plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.todosapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.todosapp"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.circleindicator)
    implementation(libs.gifdrawable)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.circleimageview)
    implementation(libs.flexbox)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}