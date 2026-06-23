plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.smsserver"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.smsserver"
        minSdk = 28
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
    buildFeatures {
        viewBinding = true
    }
    packaging {
        resources.pickFirsts.add("META-INF/io.netty.versions.properties")
        resources {
            excludes += "META-INF/INDEX.LIST"
            // Если вылезут аналогичные ошибки с другими файлами, их можно добавить сюда же, например:
            // excludes += 'META-INF/DEPENDENCIES'
        }
    }

    buildToolsVersion = "36.0.0"
}

dependencies {
    // Базовые компоненты Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // Ktor с исключением конфликтующих логгеров
    implementation("io.ktor:ktor-server-core:2.3.12") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation("io.ktor:ktor-server-netty:2.3.12") {
        exclude(group = "io.netty", module = "netty-transport-native-epoll")
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation("io.ktor:ktor-server-netty-jvm")

    // Для использования инструментов тестирования androidx
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Для обычных (локальных) тестов kotlin.test
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.0.0")

    testImplementation("junit:junit:4.13.2")
}