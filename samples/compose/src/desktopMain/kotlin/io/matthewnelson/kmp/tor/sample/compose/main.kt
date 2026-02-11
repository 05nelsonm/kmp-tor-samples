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
@file:Suppress("RedundantCompanionReference")

package io.matthewnelson.kmp.tor.sample.compose

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.matthewnelson.kmp.log.Log
import io.matthewnelson.kmp.log.sys.SysLog
import org.jetbrains.skiko.MainUIDispatcher

fun main() {
    // Using kmp-log to dispatch TorRuntime logs to both System.{out/err} and UILog
    Log.Root.install(log = SysLog.Debug)
    Log.Root.install(log = UILog(main = MainUIDispatcher))

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "compose",
        ) {
            App()
        }
    }
}
