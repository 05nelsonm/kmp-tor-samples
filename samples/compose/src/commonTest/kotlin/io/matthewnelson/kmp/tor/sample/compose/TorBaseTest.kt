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

import io.matthewnelson.kmp.tor.runtime.Action.Companion.startDaemonAsync
import io.matthewnelson.kmp.tor.runtime.Action.Companion.stopDaemonAsync
import io.matthewnelson.kmp.tor.runtime.RuntimeEvent
import io.matthewnelson.kmp.tor.runtime.core.OnEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

abstract class TorBaseTest {

    @Test
    fun givenTor_whenStart_thenStarts() = runTest {
        val tag = "TEST_OBSERVER"
        val observers = RuntimeEvent.entries().map { event ->
            event.observer(tag, OnEvent.Executor.Immediate) { data ->
                println("$event - $data")
            }
        }.toTypedArray()

        Tor.subscribe(*observers)
        currentCoroutineContext().job.invokeOnCompletion { Tor.unsubscribeAll(tag) }

        Tor.startDaemonAsync()
        withContext(Dispatchers.Default) { delay(1.seconds) }
        Tor.stopDaemonAsync()
    }
}
