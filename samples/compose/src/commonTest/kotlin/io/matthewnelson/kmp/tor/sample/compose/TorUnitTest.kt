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

import io.matthewnelson.kmp.log.Log
import io.matthewnelson.kmp.log.sys.SysLog
import io.matthewnelson.kmp.tor.runtime.Action
import io.matthewnelson.kmp.tor.runtime.Action.Companion.startDaemonAsync
import io.matthewnelson.kmp.tor.runtime.Action.Companion.stopDaemonAsync
import io.matthewnelson.kmp.tor.runtime.core.OnFailure
import io.matthewnelson.kmp.tor.runtime.core.OnSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class TorUnitTest {

    @Test
    fun givenTor_whenStart_thenStarts() = runTest {
        currentCoroutineContext().job.invokeOnCompletion {
            Tor.enqueue(Action.StopDaemon, OnFailure.noOp(), OnSuccess.noOp())
        }

        Log.Root.install(SysLog.Debug)

        try {
            Tor.startDaemonAsync()
            withContext(Dispatchers.Default) { delay(2.seconds) }
            Tor.stopDaemonAsync()
        } finally {
            Log.Root.uninstall(SysLog.Debug)
        }
    }
}
