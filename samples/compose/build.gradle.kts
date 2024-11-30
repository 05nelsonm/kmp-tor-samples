/*
 * Copyright (c) 2024 Matthew Nelson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
import io.matthewnelson.kmp.configuration.extension.container.target.TargetIosContainer
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.desktop.DesktopExtension
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("configuration")
}

private val compose: ComposeExtension get() = extensions.getByType()

// Using https://github.com/05nelsonm/gradle-kmp-configuration-plugin extension to set things
// up. You would (obviously) need to adapt things to your build setup, but this gives a rough
// idea to grok the concept.
kmpConfiguration {
    configure {
        androidApp {
            android {
                namespace = "io.matthewnelson.kmp.tor.sample.compose"
                compileSdk = 34

                sourceSets["main"].resources.srcDirs("src/commonMain/resources")

                defaultConfig {
                    applicationId = "io.matthewnelson.kmp.tor.sample.compose"
                    minSdk = 21
                    targetSdk = 34
                    versionCode = 1
                    versionName = "1.0.0"

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                packaging {
                    // Utilizing kmp-tor-resource:resource-exec-tor for Android here, so
                    // must unpack jniLibs to application's nativeLibDir on installation.
                    jniLibs.useLegacyPackaging = true

                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }

                // Setup ABI splits
                splits {
                    abi {
                        isEnable = true
                        reset()
                        include("x86", "armeabi-v7a", "arm64-v8a", "x86_64")
                        isUniversalApk = true
                    }
                }

                buildFeatures {
                    compose = true
                }
            }

            sourceSetMain {
                dependencies {
                    implementation(libs.androidx.activity.compose)
                    implementation(compose.dependencies.preview)

                    // Optional dependency for running in Android Foreground
                    // Service. Not necessary as can run background service
                    // w/o it via the `kmp-tor:runtime-service` dependency,
                    // or just use the `kmp-tor:runtime` from commonMain to
                    // do no service.
                    //
                    // See: https://github.com/05nelsonm/kmp-tor/blob/master/library/runtime-service/README.md
                    implementation(libs.kmp.tor.runtime.serviceui)
                }

                project.dependencies {
                    "debugImplementation"(compose.dependencies.uiTooling)
                }
            }

            sourceSetTest {
                dependencies {

                    // Tor binary resources for Android Unit Tests (just
                    // the jvm dependencies packaged for android)
                    implementation(libs.kmp.tor.resource.android.unit.test.tor)
                }
            }

            sourceSetTestInstrumented {
                dependencies {
                    implementation(libs.androidx.test.runner)
                }
            }

            kotlinJvmTarget = JavaVersion.VERSION_11
            compileSourceCompatibility = JavaVersion.VERSION_11
            compileTargetCompatibility = JavaVersion.VERSION_11
        }

        jvm(targetName = "desktop") {
            target {
                compose.extensions.getByType<DesktopExtension>().application {
                    mainClass = "io.matthewnelson.kmp.tor.sample.compose.MainKt"

                    // TODO: Setup excludes for distributions to minimize app size.
                    //  - .deb only needs `**/tor/native/linux-*/**
                    //  - .dmg only needs `**/tor/native/macos/**`
                    //  - .msi only needs `**/tor/native/mingw/**`
                    nativeDistributions {
                        targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                        packageName = "io.matthewnelson.kmp.tor.sample.compose"
                        packageVersion = "1.0.0"
                    }
                }
            }

            sourceSetMain {
                dependencies {
                    implementation(compose.dependencies.desktop.currentOs)

                    // Even though compose is being used, we want to use kmp-tor's
                    // OnEvent.Executor.Main type for the observers. If this was a
                    // JavaFX app, you'd want to define the JavaFX coroutine dependency.
                    implementation(libs.kotlinx.coroutines.swing)
                }
            }

            kotlinJvmTarget = JavaVersion.VERSION_11
            compileSourceCompatibility = JavaVersion.VERSION_11
            compileTargetCompatibility = JavaVersion.VERSION_11
        }

        fun <T: KotlinNativeTarget> TargetIosContainer<T>.configure() {
            target {
                binaries.framework {
                    baseName = "ComposeApp"
                    isStatic = true
                }
            }
        }

        // For iOS device some setup is needed to incorporate the LibTor.framework
        // that is expected to be present at runtime.
        // See: https://github.com/05nelsonm/kmp-tor-resource/blob/master/library/resource-frameworks-gradle-plugin/README.md
        iosArm64 { configure() }

        iosSimulatorArm64 { configure() }
        iosX64 { configure() }

        common {
            pluginIds(libs.plugins.compose.compiler.get().pluginId)
            pluginIds(libs.plugins.jetbrains.compose.get().pluginId)

            sourceSetMain {
                dependencies {
                    implementation(libs.jetbrains.lifecycle.runtime.compose)
                    implementation(libs.jetbrains.lifecycle.viewmodel)
                    implementation(compose.dependencies.runtime)
                    implementation(compose.dependencies.foundation)
                    implementation(compose.dependencies.material)
                    implementation(compose.dependencies.ui)
                    implementation(compose.dependencies.components.resources)
                    implementation(compose.dependencies.components.uiToolingPreview)

                    // TorRuntime
                    implementation(libs.kmp.tor.runtime)

                    // Pre-compiled tor binary resources to provide to TorRuntime.Environment.Build
                    //
                    // Could express them individually per target for whatever implementation type
                    // you wish to use for that platform, but being lazy here and just including both
                    // in commonMain as publications have all targets defined but only implement their
                    // respective `getOrCreate` functionality for the supported platforms (e.g. no
                    // implementation of Exec for iOS, no implementation of NoExec for Node.js).
                    implementation(libs.kmp.tor.resource.exec.tor)
                    implementation(libs.kmp.tor.resource.noexec.tor)
                }
            }

            sourceSetTest {
                dependencies {
                    implementation(kotlin("test"))
                    implementation(libs.kotlinx.coroutines.test)
                }
            }
        }
    }
}
