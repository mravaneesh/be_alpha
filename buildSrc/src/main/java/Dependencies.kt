object Versions {
    const val core = "1.10.0"
    const val appcompat = "1.7.0"
    const val androidMaterial = "1.9.0"
    const val constraintLayout = "2.2.0"

    const val testImplJunit = "4.13.2"
    const val androidTestImplJunit = "1.1.3"
    const val androidTestEspresso = "3.4.0"

    const val retrofit = "2.10.0"
    const val gsonConvertor = "2.10.0"
    const val okHttp = "4.10.0"
    const val scalerConvertor = "2.2.0"

    const val kotlinCoroutines = "1.7.0"

    const val coroutineLifecycleScope = "2.6.0"

    const val glide = "4.15.0"

    const val viewModelDeligate = "1.7.0"

    const val dagger = "2.50"
    const val hiltCompiler = "1.1.0"

    const val roomVersion = "2.5.0"

    const val swipeRefresh = "1.2.0"

    const val lottieAnimations = "4.0.0"

}

object Deps {

    const val core = "androidx.core:core-ktx:${Versions.core}"
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    const val androidMaterial = "com.google.android.material:material:${Versions.androidMaterial}"
    const val constraintLayout =
        "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"

}

object TestImplementation {

    const val junit = "junit:junit:${Versions.testImplJunit}"
}

object AndroidTestImplementation {

    const val junit = "androidx.test.ext:junit:${Versions.androidTestImplJunit}"
    const val espresso = "androidx.test.espresso:espresso-core:${Versions.androidTestEspresso}"

}


object Retrofit {
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val gsonConvertor = "com.squareup.retrofit2:converter-gson:${Versions.gsonConvertor}"
    const val okHttp = "com.squareup.okhttp3:okhttp:${Versions.okHttp}"
    const val scalersConvertors =
        "com.squareup.retrofit2:converter-scalars:${Versions.scalerConvertor}"
}

object Coroutines {
    const val coroutineCore =
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}"
    const val coroutineAndroid =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutines}"
}

object CoroutinesLifecycleScope {
    const val lifecycleViewModel =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.coroutineLifecycleScope}"
    const val lifeCycleRuntime =
        "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.coroutineLifecycleScope}"
}

object Glide {
    const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    const val annotationProcessor = "com.github.bumptech.glide:compiler:${Versions.glide}"
}

object ViewModelDelegate {
    const val viewModelDeligate = "androidx.activity:activity-ktx:${Versions.viewModelDeligate}"
}

object DaggerHilt {
    const val hilt = "com.google.dagger:hilt-android:${Versions.dagger}"
    const val hiltAndroidCompiler = "com.google.dagger:hilt-android-compiler:${Versions.dagger}"
    const val hiltCompiler = "androidx.hilt:hilt-compiler:${Versions.hiltCompiler}"
}

object Room {
    const val roomCompiler = "androidx.room:room-compiler:${Versions.roomVersion}"
    const val room = "androidx.room:room-ktx:${Versions.roomVersion}"
}

object CircularProgressBar {
    const val swipeRefresh =
        "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.swipeRefresh}"
}

object LottieAnimations {
    const val lottieAnimations = "com.airbnb.android:lottie:${Versions.lottieAnimations}"
}
