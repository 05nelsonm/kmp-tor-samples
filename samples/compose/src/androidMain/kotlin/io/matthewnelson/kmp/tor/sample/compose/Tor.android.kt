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

import io.matthewnelson.kmp.tor.common.api.ExperimentalKmpTorApi
import io.matthewnelson.kmp.tor.resource.exec.tor.ResourceLoaderTorExec
import io.matthewnelson.kmp.tor.runtime.TorRuntime
import io.matthewnelson.kmp.tor.runtime.service.TorServiceConfig
import io.matthewnelson.kmp.tor.runtime.service.TorServiceUI
import io.matthewnelson.kmp.tor.runtime.service.ui.KmpTorServiceUI

actual fun runtimeEnvironment(): TorRuntime.Environment = AndroidEnvironment

// Read documentation for further configuration
private val AndroidEnvironment: TorRuntime.Environment by lazy {
    // Roll with all the defaults.
    //
    // Note that use of ServiceConfig here does not require any Context b/c it
    // utilizes the androix.startup library under the hood. If you are using
    // the TorRuntime.Environment.Builder APIs (so not running tor in a Service),
    // you would want to use Context here to set up directory information just as
    // you would any other platform when creating the Environment.
    ServiceConfig.newEnvironment(ResourceLoaderTorExec::getOrCreate)

    // Can also utilize the NoExec JNI implementation if you don't want to
    // do process execution. Downside is that you are unable to instantiate
    // multiple instances of TorRuntime to run multiple instances of tor (within
    // a single TorService instance).
//    ServiceConfig.newEnvironment(ResourceLoaderTorNoExec::getOrCreate)
}

// Read documentation for further configuration :runtime-service dependency
// (optional or create your own TorRuntime.ServiceFactory)
private val ServiceConfig: TorServiceConfig by lazy {

    // :runtime-service-ui dependency (optional, or create your own UI)
    val uiFactory = KmpTorServiceUI.Factory(
        iconReady = R.drawable.baseline_directions_transit_filled_24,
        iconNotReady = R.drawable.baseline_no_transfer_24,
        info = TorServiceUI.NotificationInfo(
            notificationId = 615,
            channelId = "Compose KmpTor Sample",
            channelName = R.string.channel_name,
            channelDescription = R.string.channel_description,
            channelShowBadge = false,
            channelImportanceLow = false,
        ),
        block = {
            defaultConfig {
                iconData = R.drawable.baseline_departure_board_24
                enableActionRestart = true
                enableActionStop = true
                colorReady = R.color.purple

                // Customize further...
            }

            // Disable notification on-click app open (if desired)
            contentIntent = null

            // Customize further...
        },
    )

    // So UIState debug logs will also be shown...
    uiFactory.debug = true

    TorServiceConfig.Foreground.Builder(uiFactory) {
        @OptIn(ExperimentalKmpTorApi::class)
        testUseBuildDirectory = true

        // Customize further...
    }
}
