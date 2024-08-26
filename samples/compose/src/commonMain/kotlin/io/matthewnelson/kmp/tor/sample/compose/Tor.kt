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
@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package io.matthewnelson.kmp.tor.sample.compose

import io.matthewnelson.kmp.tor.runtime.RuntimeEvent
import io.matthewnelson.kmp.tor.runtime.TorRuntime

// Use your favorite dependency injection here, if desired.
expect fun runtimeEnvironment(): TorRuntime.Environment

// Read documentation for further options
// Use your favorite dependency injection framework here
val Tor: TorRuntime by lazy {
    TorRuntime.Builder(runtimeEnvironment()) {

        // Log all output to sys out
        RuntimeEvent.entries().forEach { entry ->
            // ERROR observer **MUST** be present for
            // UncaughtException, otherwise may cause crash.
            if (entry is RuntimeEvent.ERROR) {
                observerStatic(entry) { t -> t.printStackTrace() }
            } else {

                // Just toString everything else...
                observerStatic(entry) { event -> println(event.toString()) }
            }
        }

        config { environment ->
            // Configure further...
        }
    }
}
