import java.util.Properties


plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.home_ui"
    compileSdk = 35

    defaultConfig {
        minSdk = 30

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        val awsProps = Properties()
        val propsFile = file("aws_credentials.properties")
        if (propsFile.exists()) {
            awsProps.load(propsFile.inputStream())
        }
        buildConfigField("String", "AWS_ACCESS_KEY", "\"${awsProps["AWS_ACCESS_KEY"]}\"")
        buildConfigField("String", "AWS_SECRET_KEY", "\"${awsProps["AWS_SECRET_KEY"]}\"")
        buildConfigField("String", "AWS_BUCKET_NAME", "\"${awsProps["AWS_BUCKET_NAME"]}\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(project(":common:utils"))
    implementation(project(":common:ui"))
    implementation(project(":home:home_domain"))
    implementation(project(":home:home_data"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx") // Firebase Authentication
    implementation("com.google.firebase:firebase-firestore-ktx") // Firestore for user data

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")

    implementation("androidx.activity:activity-ktx:1.10.0")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.6")

    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")

    implementation("com.amazonaws:aws-android-sdk-s3:2.22.+")
    implementation("com.amazonaws:aws-android-sdk-core:2.22.+")


    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.tbuonomo:dotsindicator:4.3")

}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}