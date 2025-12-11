plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.learning_app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.learning_app"
        minSdk = 24
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
    //Thư viện cho BottomNavigationView
    implementation("com.google.android.material:material:1.10.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")

    // Thư viện cho Avatar (ảnh tròn)
    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // == THƯ VIỆN FIREBASE ==
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Authentication (Đăng nhập/Đăng ký)
    implementation("com.google.firebase:firebase-auth")

    // Cloud Firestore (Cơ sở dữ liệu)
    implementation("com.google.firebase:firebase-firestore")

    // Storage (Lưu ảnh)
    implementation("com.google.firebase:firebase-storage")
}