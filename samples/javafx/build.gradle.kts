/*
 * Copyright (c) 2024 Matthew Nelson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
plugins {
    application
    alias(libs.plugins.javafx)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// TODO: Setup excludes for distributions to minimize app size.
//  - .deb only needs `**/tor/native/linux-*/**
//  - .dmg only needs `**/tor/native/macos/**`
//  - .msi only needs `**/tor/native/mingw/**`

application {
    mainClass.set("io.matthewnelson.kmp.tor.sample.javafx.App")
}

javafx {
    modules("javafx.base", "javafx.controls", "javafx.graphics")
}

dependencies {
    implementation(libs.kmp.tor.runtime)

    // Alternatively, could use -exec dependency for process execution.
    // Alternatively, could use the -gpl variant for GPL'd applications.
    implementation(libs.kmp.tor.resource.noexec.tor)

    // To support use of OnEvent.Executor.Main for Observers (optional, will default to Immediate).
    implementation(libs.kotlinx.coroutines.javafx)
}
