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
    jacoco
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
                val keystoreFile = File(project.layout.buildDirectory.get().asFile.path, "tmp/release.jks")
                keystoreFile.parentFile.mkdirs()
                keystoreFile.delete()
                keystoreFile.createNewFile()

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
    compileSdk = 36

    defaultConfig {
        applicationId = "org.dsh.personal.sudoku"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "0.9.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "DEVELOPER_NAME", "\"Dmytro Shulha\"")
    }


    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
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
    testOptions {
        unitTests.all {
            testCoverage {

            }
        }
    }
}

tasks.register("jacocoTestReport", JacocoReport::class) {
    dependsOn("testDebugUnitTest", "createDebugUnitTestCoverageReport")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(true)
    }

    val mainSrc = "${projectDir}/src/main/java"

    val kotlinClasses = fileTree("${project.layout.buildDirectory}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    // For Java classes (if you have them mixed or use Java extensively)
    val javaClasses = fileTree("${project.layout.buildDirectory}/intermediates/javac/debug/classes") {
        exclude(fileFilter)
    }
     val kspClasses = fileTree("${project.layout.buildDirectory}/generated/ksp/debug/kotlin") {
        exclude(fileFilter)
     }
    classDirectories.setFrom(files(kotlinClasses, javaClasses , kspClasses))
    sourceDirectories.setFrom(files(mainSrc))

    executionData.setFrom(
        fileTree(project.layout.buildDirectory) {
            // Common locations for JaCoCo .exec files
            include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec") // AGP default
            include("jacoco/testDebugUnitTest.exec") // Gradle default
        }
    )
}

val fileFilter = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "android/**/*.*",

    "**/*\$Lambda$*.*", // Ignore Kotlin lambdas
    "**/*\$inlined$*.*", // Ignore Kotlin inlined functions
    "**/di/**",
    "**/database/**",

    "**/*ComposableSingletons*",
    // Module specific exclusions
    "org/dsh/personal/sudoku/data/di/**",
    "org/dsh/personal/sudoku/theme/**",
    "org/dsh/personal/sudoku/presentation/**",
    "org/dsh/personal/sudoku/PersonalApplication*",
    "org/dsh/personal/sudoku/ui/**", // Assuming UI specific, non-business logic
    "org/dsh/personal/sudoku/navigation/**",
)


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
    classpath.setFrom(files(project.layout.buildDirectory.toString() + "/tmp/kotlin-classes/debug"))
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
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    detektPlugins(libs.detekt.formatting)
}
