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
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.desktop.DesktopExtension
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("configuration")
}

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
                }

                packaging {
                    jniLibs.useLegacyPackaging = true

                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }

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
                }

                project.dependencies {
                    "debugImplementation"(compose.dependencies.uiTooling)
                }
            }

            kotlinJvmTarget = JavaVersion.VERSION_11
            compileSourceCompatibility = JavaVersion.VERSION_11
            compileTargetCompatibility = JavaVersion.VERSION_11
        }

        jvm("desktop") {
            target {
                compose.extensions.getByType<DesktopExtension>().application {
                    mainClass = "io.matthewnelson.kmp.tor.sample.compose.MainKt"

                    nativeDistributions {
                        targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                        packageName = "io.matthewnelson.kmp.tor.sample.compose"
                        packageVersion = "1.0.0"
                    }
                }
            }

            sourceSetMain {
                dependencies {
                    implementation(libs.kotlinx.coroutines.swing)
                    implementation(compose.dependencies.desktop.currentOs)
                }
            }

            kotlinJvmTarget = JavaVersion.VERSION_11
            compileSourceCompatibility = JavaVersion.VERSION_11
            compileTargetCompatibility = JavaVersion.VERSION_11
        }

        iosAll()

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
                }
            }
        }

        kotlin {
            targets.filterIsInstance<KotlinNativeTarget>().forEach { target ->
                if (!target.konanTarget.family.isAppleFamily) return@forEach

                target.binaries.framework {
                    baseName = "ComposeApp"
                    isStatic = true
                }
            }
        }
    }
}

private val compose: ComposeExtension get() = extensions.getByType()
