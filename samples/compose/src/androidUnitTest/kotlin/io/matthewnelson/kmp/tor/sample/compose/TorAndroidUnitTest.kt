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

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.AfterClass
import org.junit.BeforeClass

@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
class TorAndroidUnitTest: TorBaseTest() {

    companion object {

        private val mainThreadSurrogate = newSingleThreadContext("UI thread")

        @JvmStatic
        @BeforeClass
        fun setupClazz() {
            Dispatchers.setMain(mainThreadSurrogate)
        }

        @JvmStatic
        @AfterClass
        fun teardownClazz() {
            Dispatchers.resetMain()
            mainThreadSurrogate.close()
        }
    }
}
