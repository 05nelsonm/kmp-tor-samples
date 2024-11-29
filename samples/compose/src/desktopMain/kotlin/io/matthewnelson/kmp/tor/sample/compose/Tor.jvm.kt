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
package io.matthewnelson.kmp.tor.sample.compose

import io.matthewnelson.kmp.file.SysTempDir
import io.matthewnelson.kmp.file.resolve
import io.matthewnelson.kmp.file.toFile
import io.matthewnelson.kmp.tor.resource.exec.tor.ResourceLoaderTorExec
import io.matthewnelson.kmp.tor.runtime.TorRuntime

actual fun runtimeEnvironment(): TorRuntime.Environment = JvmEnvironment

// Read documentation for further options
private val JvmEnvironment: TorRuntime.Environment by lazy {
    val rootDir = System.getProperty("user.home")
        ?.ifBlank { null }
        ?.toFile()
        ?: SysTempDir

    val appDir = rootDir.resolve("kmp_tor_samples").resolve("compose")

    TorRuntime.Environment.Builder(
        workDirectory = appDir.resolve("kmptor"),
        cacheDirectory = appDir.resolve("cache").resolve("kmptor"),
        loader = ResourceLoaderTorExec::getOrCreate,
    )
}
