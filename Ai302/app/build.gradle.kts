plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    //id("com.google.devtools.ksp")
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.ai302.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ai302.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += "armeabi"
            //abiFilters += listOf("arm64-v8a", "armeabi-v7a","armeabi","x86","x86_64")
        }
    }

    //签名打包
    signingConfigs {
        create("keyStore") {
            keyAlias = "proxy"
            keyPassword = "123456"
            storeFile = file("proxy302.jks")
            storePassword = "123456"
        }
    }

    buildTypes {
        release {
            isShrinkResources = false
            isMinifyEnabled = true//开启代码混淆
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("keyStore")
        }

        debug {
//            applicationIdSuffix = "debug"
            isMinifyEnabled = false

            isDebuggable = true
            isJniDebuggable = true
        }
    }

    buildFeatures {

        viewBinding = true

    }

    kotlinOptions {
        jvmTarget = "1.8"
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

    sourceSets {

        getByName("main") {
            jniLibs.srcDirs("libs")  // 指定 jniLibs 目录
        }
    }

}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("com.github.CymChad:BaseRecyclerViewAdapterHelper:3.0.4")
    //popup window
    implementation("com.github.zyyoona7:EasyPopup:1.1.2")
    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.6.3")
    // Retrofit 的 Moshi 转换器
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    //implementation("com.squareup.retrofit2:adapter-kotlin-coroutines:2.9.0") // 关键依赖
    //okhttp
    implementation("com.squareup.okhttp3:okhttp:3.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    //rxjava
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.6.3")
    implementation("io.reactivex.rxjava2:rxjava:2.2.19")
    implementation("io.reactivex.rxjava2:rxandroid:2.0.1")
    //dataStore
    implementation("androidx.datastore:datastore-preferences:1.1.4")

    // Lifecycle 依赖
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    //kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0")
    //room
    implementation("androidx.room:room-runtime:2.4.3")
    kapt("androidx.room:room-compiler:2.4.0")
    implementation("com.google.code.gson:gson:2.8.0")
    implementation("androidx.room:room-ktx:2.4.3")
    //recycleView item侧滑栏
    implementation("com.github.mcxtzhang:SwipeDelMenuLayout:V1.2.1")

    //动态获取权限
    implementation("pub.devrel:easypermissions:3.0.0")

    //webView数据桥接
    implementation("com.github.lzyzsd:jsbridge:1.0.4")


    //markDown->html
//    implementation("com.atlassian.commonmark:commonmark:0.17.0")  //所以应该是0.13.0？
//    implementation("com.atlassian.commonmark:commonmark-ext-gfm-tables:0.17.0") // 支持表格

    // 显式指定 CommonMark 版本（与 Markwon 兼容）
//    implementation("org.commonmark:commonmark:0.24.0")

    // Markwon核心库（最新版本查看https://github.com/noties/Markwon）
    implementation("io.noties.markwon:core:4.6.2")
    // 常用扩展插件
    implementation("io.noties.markwon:html:4.5.0")
    implementation("io.noties.markwon:ext-strikethrough:4.6.0")
    implementation("io.noties.markwon:ext-tasklist:4.6.0")
    implementation("io.noties.markwon:ext-tables:4.6.0")
    implementation("io.noties.markwon:linkify:4.6.2")
    // LaTeX 数学公式渲染插件
    implementation("io.noties.markwon:ext-latex:4.6.2")
    // JLatexMath 引擎（用于解析和渲染 LaTeX）
    //implementation("org.scilab.forge:jlatexmath:1.0.6")//和上面的latex库不兼容


    // 图片加载（使用Glide）
    implementation("io.noties.markwon:image-glide:4.6.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")


    // PhotoView 图片缩放库（用于放大预览）
    implementation("com.github.chrisbanes:PhotoView:2.3.0")


    //TTS
    implementation("com.github.cczhr:voice-tts:1.0.3")

    // 引入 LeakCanary 依赖
//    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.11")
//    releaseImplementation("com.squareup.leakcanary:leakcanary-android-no-op:2.11")

}