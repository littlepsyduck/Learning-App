plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.learning_app"
    // SỬA Ở ĐÂY: Cú pháp đúng là gán số trực tiếp.
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.learning_app"
        minSdk = 24

        // SỬA Ở ĐÂY: Đổi từ 34 thành 36
        targetSdk = 36

        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // SỬA Ở ĐÂY: Dùng ("...") thay vì '...' trong file .kts
    //Thư viện cho BottomNavigationView
    implementation("com.google.android.material:material:1.10.0")

    // SỬA Ở ĐÂY: Dùng ("...") thay vì '...' trong file .kts
    // Thư viện cho Avatar (ảnh tròn)
    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}