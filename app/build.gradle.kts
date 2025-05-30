import java.util.Base64
import java.io.File
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialize)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.detekt)
}

android {
    signingConfigs {
        create("release") {
            val keystoreBase64 = System.getenv("ANDROID_KEYSTORE_BASE64")
            val keystorePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
            val keyAliasName = System.getenv("ANDROID_KEY_ALIAS")
            val keyPasswordValue = System.getenv("ANDROID_KEY_PASSWORD")

            if (keystoreBase64 != null && keystorePassword != null && keyAliasName != null && keyPasswordValue != null) {
                // Define the path for the decoded keystore file in the build directory
                val keystoreFile = File(project.buildDir, "tmp/release.jks")
                keystoreFile.parentFile.mkdirs()
                keystoreFile.delete()

                val decodedKeystoreBytes = Base64.getDecoder().decode(keystoreBase64)
                keystoreFile.writeBytes(decodedKeystoreBytes)

                storeFile = keystoreFile
                this.storePassword = keystorePassword
                this.keyAlias = keyAliasName
                this.keyPassword = keyPasswordValue
            } else {
                val localProps = Properties()
                val localPropsFile = rootProject.file("local.properties")

                if (localPropsFile.exists() && localPropsFile.isFile) {
                    FileInputStream(localPropsFile).use { fis ->
                        localProps.load(fis)
                    }
                }

                val storeFilePathLocal = localProps.getProperty("android.injected.signing.store.file")
                val storePasswordLocal = localProps.getProperty("android.injected.signing.store.password")
                val keyAliasLocal = localProps.getProperty("android.injected.signing.key.alias")
                val keyPasswordLocal = localProps.getProperty("android.injected.signing.key.password")

                if (storeFilePathLocal != null && storePasswordLocal != null && keyAliasLocal != null && keyPasswordLocal != null) {
                    storeFile = File(storeFilePathLocal) // Directly use the path from local.properties
                    this.storePassword = storePasswordLocal
                    this.keyAlias = keyAliasLocal
                    this.keyPassword = keyPasswordLocal
                    println("Using signing configuration from local.properties.")
                } else {
                    println("WARNING: Signing information not found in environment variables or local.properties. Release build will not be signed or may fail.")
                }
            }
        }
    }
    namespace = "org.dsh.personal.sudoku"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.dsh.personal.sudoku"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "0.9.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "DEVELOPER_NAME", "\"Dmytro Shulha\"")
    }


    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

detekt {
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    baseline = file("$rootDir/config/detekt/detekt-baseline.xml")
    ignoreFailures = false

}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    setSource(files(project.projectDir)) // Or more specific source sets
    classpath.setFrom(files(project.buildDir.toString() + "/tmp/kotlin-classes/debug"))
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(false)
        sarif.required.set(true)
    }
}

dependencies {
    implementation(libs.bundles.koinForAndroid)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.androidx.compose.icons.core)
    implementation(libs.androidx.compose.icons.extented)
    implementation(libs.data.dataStorePreferences)
    implementation(libs.kotlinx.serialization.json)

    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.sqlite.bundled)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.window)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    detektPlugins(libs.detekt.formatting)
}