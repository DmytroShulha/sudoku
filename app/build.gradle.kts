import com.android.build.api.dsl.ApplicationExtension
import java.util.Base64
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialize)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.detekt)
    jacoco
}

fun loadVersionProperties(project: Project): Properties {
    val properties = Properties()
    val propertiesFile = project.file("version.properties")

    if (propertiesFile.exists()) {
        FileInputStream(propertiesFile).use { fis ->
            properties.load(fis)
        }
    } else {
        project.logger.info("WARNING: version.properties file not found in ${project.projectDir}. Using default versions.")
    }
    return properties
}

configure<ApplicationExtension> {
    namespace = "org.dsh.personal.sudoku"
    compileSdk = 36

    signingConfigs {
        create("release") {
            val keystoreBase64 = System.getenv("ANDROID_KEYSTORE_BASE64")
            val keystorePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
            val keyAliasName = System.getenv("ANDROID_KEY_ALIAS")
            val keyPasswordValue = System.getenv("ANDROID_KEY_PASSWORD")

            if (keystoreBase64 != null && keystorePassword != null && keyAliasName != null && keyPasswordValue != null) {
                // Using projectLayout for modern API compatibility
                val keystoreFile = layout.buildDirectory.file("tmp/release.jks").get().asFile
                keystoreFile.parentFile.mkdirs()

                val decodedKeystoreBytes = Base64.getDecoder().decode(keystoreBase64)
                keystoreFile.writeBytes(decodedKeystoreBytes)

                storeFile = keystoreFile
                storePassword = keystorePassword
                keyAlias = keyAliasName
                keyPassword = keyPasswordValue
            } else {
                val localProps = Properties()
                val localPropsFile = rootProject.file("local.properties")

                if (localPropsFile.exists()) {
                    localPropsFile.inputStream().use { localProps.load(it) }
                }

                val storeFilePathLocal = localProps.getProperty("android.injected.signing.store.file")
                if (storeFilePathLocal != null) {
                    storeFile = file(storeFilePathLocal)
                    storePassword = localProps.getProperty("android.injected.signing.store.password")
                    keyAlias = localProps.getProperty("android.injected.signing.key.alias")
                    keyPassword = localProps.getProperty("android.injected.signing.key.password")
                }
            }
        }
    }

    defaultConfig {
        applicationId = "org.dsh.personal.sudoku"
        minSdk = 28
        targetSdk = 36

        val versionProps = loadVersionProperties(project)
        versionCode = versionProps.getProperty("appVersionCode", "1").toInt()
        versionName = versionProps.getProperty("appVersionName", "1")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "DEVELOPER_NAME", "\"Dmytro Shulha\"")
    }

    buildTypes {
        debug {
            enableAndroidTestCoverage = true
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

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    //noinspection WrongGradleMethod
    kotlin {
        compilerOptions {
            freeCompilerArgs.add("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
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
    ignoreFailures = true

}

tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
    setSource(files(project.projectDir))
    classpath.setFrom(files(project.layout.buildDirectory.toString() + "/tmp/kotlin-classes/debug"))
    excludes.addAll(listOf("**/resources/**", "*.gradle.kts", "**/test/**", "**/androidTest/**"))
    reports {
        html.required.set(true)
        sarif.required.set(true)
    }
}

dependencies {
    implementation(libs.bundles.koinForAndroid)
    implementation(libs.bundles.nav3)
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
