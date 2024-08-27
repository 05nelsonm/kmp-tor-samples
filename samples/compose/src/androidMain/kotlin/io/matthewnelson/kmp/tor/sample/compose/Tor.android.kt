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

import io.matthewnelson.kmp.tor.resource.tor.TorResources
import io.matthewnelson.kmp.tor.runtime.TorRuntime
import io.matthewnelson.kmp.tor.runtime.service.TorServiceConfig
import io.matthewnelson.kmp.tor.runtime.service.TorServiceUI
import io.matthewnelson.kmp.tor.runtime.service.ui.KmpTorServiceUI


actual fun runtimeEnvironment(): TorRuntime.Environment = AndroidEnvironment

// Read documentation for further configuration
private val AndroidEnvironment: TorRuntime.Environment by lazy {
    // Roll with all the defaults
    ServiceConfig.newEnvironment { dir -> TorResources(dir) }
}

// Read documentation for further configuration
// :runtime-service dependency (optional or create your own TorRuntime.ServiceFactory)
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

            // Customize further...
        },
    )

    // So UI logs will also be present...
    uiFactory.debug = true

    TorServiceConfig.Foreground.Builder(uiFactory) {
        // Customize further...
    }
}
