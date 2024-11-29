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

import io.matthewnelson.kmp.file.*
import io.matthewnelson.kmp.tor.resource.noexec.tor.ResourceLoaderTorNoExec
import io.matthewnelson.kmp.tor.runtime.TorRuntime
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSLibraryDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual fun runtimeEnvironment(): TorRuntime.Environment = IosEnvironment

// Read documentation for further options
private val IosEnvironment: TorRuntime.Environment by lazy {
    // ../data/Library
    val library = NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, true)
        .firstOrNull()
        ?.toString()
        ?.ifBlank { null }
        ?.toFile()
        ?: "".toFile().absoluteFile.resolve("Library")

    // ../data/Library/Caches
    val caches = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
        .firstOrNull()
        ?.toString()
        ?.ifBlank { null }
        ?.toFile()
        ?: library.resolve("Caches")

    TorRuntime.Environment.Builder(
        workDirectory = library.resolve("kmptor"),
        cacheDirectory = caches.resolve("kmptor"),
        loader = ResourceLoaderTorNoExec::getOrCreate,
    )
}
