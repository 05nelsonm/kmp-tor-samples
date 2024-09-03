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
import io.matthewnelson.kmp.tor.runtime.core.OnEvent

// Use your favorite dependency injection here, if desired.
expect fun runtimeEnvironment(): TorRuntime.Environment

const val LOG_HOLDER_NAME = "default"

// Read documentation for further options
// Use your favorite dependency injection framework here
val Tor: TorRuntime by lazy {
    TorRuntime.Builder(
        environment = runtimeEnvironment().also { it.debug = true },
        block = {

            // TorRuntime.Environment.BuilderScope.defaultExecutor auto-defaults
            // to OnEvent.Executor.Main whenever Dispatchers.Main is available
            // on the system (which it is here for all platforms).
            //
            // But just for expressiveness, define here individually for all
            // observers b/c the LogItem.Holder is not implemented in a thread
            // safe manner.
            val executor = OnEvent.Executor.Main

            // Pipe all logs to UI
            val logs = LogItem.Holder.getOrCreate(LOG_HOLDER_NAME)

            RuntimeEvent.entries().forEach { event ->
                // ERROR observer **MUST** be present for
                // UncaughtException, otherwise may cause crash.
                if (event is RuntimeEvent.ERROR) {
                    observerStatic(event, executor) { t ->
                        logs.add(event, t.stackTraceToString())
                    }
                } else {

                    // Just toString everything else...
                    observerStatic(event, executor) { data ->
                        logs.add(event, data.toString())
                    }
                }
            }

            config { environment ->
                // Configure further...
            }
        },
    )
}
