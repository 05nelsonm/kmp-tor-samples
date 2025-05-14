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

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import io.matthewnelson.kmp.tor.runtime.RuntimeEvent
import kotlinx.coroutines.*
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

@Immutable
class LogItem private constructor(
    @JvmField
    val id: Long,
    @JvmField
    val event: RuntimeEvent<*>,
    @JvmField
    val data: String,
) {

    override fun equals(other: Any?): Boolean = other is LogItem && other.id == id
    override fun hashCode(): Int = id.hashCode()
    override fun toString(): String = data

    /**
    * Nothing thread safe about this. Must be utilized from UI thread.
    * */
    class Holder private constructor(
        @JvmField
        val name: String,
    ) {

        val items = mutableStateOf(ArrayDeque<LogItem>(), neverEqualPolicy())
        private val main = CoroutineScope(SupervisorJob() + (UI_DISPATCHER ?: Dispatchers.Main))

        private var id: Long = 0

        fun add(event: RuntimeEvent<*>, data: String) {
            main.launch {
                items.value = items.value.let { items ->
                    val id = id++
                    if (items.size > MAX_ITEMS) items.removeFirst()
                    items.add(LogItem(id, event, data))
                    items
                }
            }
        }

        companion object {

            private val instances = HashMap<String, Holder>(1, 1.0f)

            @JvmStatic
            fun getOrCreate(name: String): Holder {
                var instance = instances[name]
                if (instance == null) {
                    instance = Holder(name).also { instances[name] = it }
                }
                return instance
            }

            private const val MAX_ITEMS = 300
        }
    }
}
