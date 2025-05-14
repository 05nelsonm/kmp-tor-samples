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
plugins {
    alias(libs.plugins.android.app) apply(false)
    alias(libs.plugins.android.library) apply(false)
    alias(libs.plugins.compose.compiler) apply(false)
    alias(libs.plugins.jetbrains.compose) apply(false)
    alias(libs.plugins.kotlin.multiplatform) apply(false)

    // For Java & KotlinMultiplatform/Jvm this is for stripping out unused compilations
    // of tor to reduce application binary size by keeping only the host/architecture
    // necessary for that distribution.
    // See: https://github.com/05nelsonm/kmp-tor-resource/blob/master/library/resource-filterjar-gradle-plugin/README.md
    alias(libs.plugins.kmp.tor.resource.filterjar) apply(false)

    // For iOS device some setup is needed to incorporate the LibTor.framework
    // that is expected to be present at runtime.
    // See: https://github.com/05nelsonm/kmp-tor-resource/blob/master/library/resource-frameworks-gradle-plugin/README.md
    alias(libs.plugins.kmp.tor.resource.frameworks)
}
