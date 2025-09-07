plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")

    // Apply the Application plugin to add support for building an executable JVM application.
    application
}

dependencies {
    // Project "app" depends on project "utils". (Project paths are separated with ":", so ":utils" refers to the top-level "utils" project.)
    implementation(project(":utils"))
    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinxCoroutinesTest)

    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.kotlinReflect)

    testImplementation(libs.mockkCore)
    testImplementation(libs.bundles.kotest)
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "com.purestation.app.AppKt"
}

// 이 옵션이 없어도 ./gradlew test시 kotest 부분 실행됨
//tasks.test {
//    useJUnitPlatform()
//}
