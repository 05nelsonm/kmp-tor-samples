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

import io.matthewnelson.kmp.file.resolve
import io.matthewnelson.kmp.tor.runtime.RuntimeEvent
import io.matthewnelson.kmp.tor.runtime.TorListeners
import io.matthewnelson.kmp.tor.runtime.TorRuntime
import io.matthewnelson.kmp.tor.runtime.core.OnEvent
import io.matthewnelson.kmp.tor.runtime.core.TorEvent
import io.matthewnelson.kmp.tor.runtime.core.config.TorOption
import io.matthewnelson.kmp.tor.runtime.core.net.Port
import io.matthewnelson.kmp.tor.runtime.core.net.Port.Companion.toPort
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.concurrent.Volatile
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

// Use your favorite dependency injection here, if desired.
expect fun runtimeEnvironment(): TorRuntime.Environment

const val LOG_HOLDER_NAME = "default"

// Fake HttpClient used as an example. Realistically this would be something
// like ktor or OkHttp, etc.
interface SomeHttpClient {

    companion object {

        // Helper to access TorSocksClient.get (which is private object).
        suspend fun get(
            timeout: Duration = 100.milliseconds,
        ): SomeHttpClient = TorSocksClient.get(timeout)
    }
}

// Example for whatever HTTP client you want to use to make network requests.
// Can also extend RuntimeEvent.Observer, but this will be used to add as static
// observer via TorRuntime.BuilderScope.observerStatic, so just extending OnEvent.
private object TorSocksClient: OnEvent<TorListeners> {

    // Define private clear net client for entirety of application runtime
    // to build tor client off of (so not repeatedly opening closing backing
    // threads).
    private val clearNetClient = object : SomeHttpClient {}

    @Volatile
    private var socksClient: SomeHttpClient? = null

    @Throws(TimeoutCancellationException::class, CancellationException::class)
    suspend fun get(timeout: Duration = 100.milliseconds): SomeHttpClient = withTimeout(timeout) {
        var c = socksClient
        while (c == null) {
            delay(5.milliseconds)
            c = socksClient
        }
        c
    }

    override fun invoke(it: TorListeners) {
        val sa = it.socks.firstOrNull()

        if (sa == null) {
            // Teardown
            //
            // Tor was shutdown, DisableNetwork set to `true`, or SocksPort
            // was reset to disabled which closed listeners.
            socksClient = null
        } else {
            // Setup

//            socksClient = clearNetClient.newBuilder {
//                // define proxy
//            }
        }
    }
}

// Read documentation for further options
// Use your favorite dependency injection framework here, if desired.
val Tor: TorRuntime by lazy {
    TorRuntime.Builder(runtimeEnvironment()) {

        // TorRuntime.Environment.BuilderScope.defaultExecutor auto-defaults
        // to OnEvent.Executor.Main whenever Dispatchers.Main is available
        // on the system (which it is here for all platforms). LogItem.Holder
        // has its own scope using Dispatchers.Main.immediate when adding the
        // events, so here we can express individually for all observers to
        // use Immediate instead of whatever the default is.
        val executor = OnEvent.Executor.Immediate

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

        // Integrate more application code via Observer API...
        observerStatic(RuntimeEvent.LISTENERS, TorSocksClient)

        config { environment ->

            // The default configuration that TorRuntime has will always define
            // SocksPort 9050 (if not defined here), and reassign to "auto" if 9050 is
            // unavailable on the device. For Application's, the safest thing would be to
            // always use "auto" and let tor choose the port for you.
            //
            // The RuntimeEvent.LISTENERS observer API allows for setup/teardown of an HTTP
            // client with the well-defined SocketAddress for the proxy to use for it.
            TorOption.__SocksPort.configure { auto() }

            try {
                // As an example for declaring unix domain socket instead of TCP port
                TorOption.__SocksPort.configure {
                    unixSocket(environment.workDirectory.resolve("socks.sock"))
                }
            } catch (_: UnsupportedOperationException) {
                // Not supported by the platform (i.e. Windows or using JDK 15-)...
            }

            // Enable tunnel port
            TorOption.HTTPTunnelPort.configure { auto() }

            // Configure further...
        }

        config { environment ->
            // Define a HiddenService (no need to wrap tryConfigure with try/catch
            // block b/c the minimum requirements for BuilderScopeHS are being met).
            TorOption.HiddenServiceDir.tryConfigure {
                directory(environment.workDirectory.resolve("ex_hs"))
                version(3)
                port(virtual = Port.HTTP) {
                    try {
                        target(unixSocket = environment.workDirectory.resolve("hs.sock"))
                    } catch (_: UnsupportedOperationException) {
                        // Not supported by the platform (i.e. Windows or using JDK 15-)...
                        target(port = 8083.toPort())
                    }
                }
            }
        }

        // Even though these are not "required" for our implementation to function, defining
        // them here will always add them to all TorCmd.SetEvents commands for the control
        // connection. All Asynchronous TorEvent will be visible in the sample app via TorCtrl
        // debug logs (which is why no static observers for TorEvent are defined here).
        required(TorEvent.ERR)
        required(TorEvent.WARN)

        // Configure further...

    }.apply { environment().debug = true }
}
