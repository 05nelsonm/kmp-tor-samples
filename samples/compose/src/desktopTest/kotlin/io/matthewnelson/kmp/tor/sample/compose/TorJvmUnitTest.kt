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

import io.matthewnelson.kmp.file.toFile
import org.junit.AfterClass
import org.junit.BeforeClass

class TorJvmUnitTest: TorBaseTest() {

    companion object {

        private val HOME = System.getProperty("user.home")

        @JvmStatic
        @BeforeClass
        fun setupClazz() {
            System.setProperty("user.home", "".toFile().absoluteFile.resolve("build/kmp_tor_jvm_test").path)
        }

        @JvmStatic
        @AfterClass
        fun tearDownClazz() {
            System.setProperty("user.home", HOME)
        }
    }
}
