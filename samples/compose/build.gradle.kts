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
import io.matthewnelson.kmp.tor.resource.filterjar.KmpTorResourceFilterJarExtension
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
                compileSdk = 35

                sourceSets["main"].resources.srcDirs("src/commonMain/resources")

                defaultConfig {
                    applicationId = "io.matthewnelson.kmp.tor.sample.compose"
                    minSdk = 21
                    targetSdk = 35
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
                    // See: https://kmp-tor.matthewnelson.io/library/runtime-service-ui/index.html
                    implementation(libs.kmp.tor.runtime.serviceui)

                    // Tor executable resources
                    implementation(libs.kmp.tor.resource.exec.tor)
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

            kotlinJvmTarget = JavaVersion.VERSION_1_8
            compileSourceCompatibility = JavaVersion.VERSION_1_8
            compileTargetCompatibility = JavaVersion.VERSION_1_8
        }

        jvm(targetName = "desktop") {

            // See: https://github.com/05nelsonm/kmp-tor-resource/blob/master/library/resource-filterjar-gradle-plugin/README.md
            pluginIds(libs.plugins.kmp.tor.resource.filterjar.get().pluginId)

            sourceSetMain {
                dependencies {
                    implementation(compose.dependencies.desktop.currentOs)

                    // Tor executable resources
                    implementation(libs.kmp.tor.resource.exec.tor)
                }
            }

            sourceSetTest {
                dependencies {
                    // So that Dispatchers.Main will be available for tests
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
                    binaryOption("bundleId", "io.matthewnelson.kmp.tor.sample.compose.ComposeApp")
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
                    implementation(compose.dependencies.runtime)
                    implementation(compose.dependencies.foundation)
                    implementation(compose.dependencies.material)
                    implementation(compose.dependencies.ui)
                    implementation(compose.dependencies.components.resources)
                    implementation(compose.dependencies.components.uiToolingPreview)

                    // TorRuntime
                    implementation(libs.kmp.tor.runtime)
                }
            }

            sourceSetTest {
                dependencies {
                    implementation(kotlin("test"))
                    implementation(libs.kotlinx.coroutines.test)
                }
            }
        }

        // Tor non-executable resources for iOS (Exec not available)
        kotlin {
            sourceSets.findByName("iosMain")?.dependencies {
                implementation(libs.kmp.tor.resource.noexec.tor)
            }
        }

        // Configure extensions for desktop
        kotlin {
            if (targets.findByName("desktop") == null) return@kotlin

            compose.extensions.getByType<DesktopExtension>().application {
                mainClass = "io.matthewnelson.kmp.tor.sample.compose.MainKt"

                nativeDistributions {
                    targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
                    packageName = "io.matthewnelson.kmp.tor.sample.compose"
                    packageVersion = "1.0.0"

                    // https://github.com/05nelsonm/kmp-process/issues/139
                    modules("java.management")
                }
            }

            // Strip out all compilations of tor but the current host & architecture
            // See: https://github.com/05nelsonm/kmp-tor-resource/blob/master/library/resource-filterjar-gradle-plugin/README.md
            project.extensions.configure<KmpTorResourceFilterJarExtension>("kmpTorResourceFilterJar") {
                logging.set(true)
                keepTorCompilation(host = "current", arch = "current")
            }
        }
    }
}
