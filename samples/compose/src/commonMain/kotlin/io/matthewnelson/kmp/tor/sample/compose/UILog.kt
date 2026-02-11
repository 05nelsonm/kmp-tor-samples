/*
 * Copyright (c) 2026 Matthew Nelson
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
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import io.matthewnelson.kmp.log.Log
import io.matthewnelson.kmp.tor.runtime.RuntimeEvent
import io.matthewnelson.kmp.tor.runtime.TorRuntime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile
import kotlin.jvm.JvmField

/**
 * Utilizing `kmp-log` in order to convert all [Log.Logger] logs from [TorRuntime] to display.
 * */
class UILog(private val main: CoroutineDispatcher): Log(uid = UID, min = Level.Debug) {

    companion object {

        const val UID = "UILog"

        // For our instance of TorRuntime to use for Log.Logger.domain
        // when piping RuntimeEvent to its Log.Logger
        const val DOMAIN = "sample:compose"

        private const val MAX_ITEMS = 300
    }

    @Immutable
    class Item(
        @JvmField
        val id: Long,
        @JvmField
        val event: RuntimeEvent<*>,
        @JvmField
        val data: String,
    ) {
        override fun equals(other: Any?): Boolean = other is Item && other.id == id
        override fun hashCode(): Int = id.hashCode()
        override fun toString(): String = data
    }

    private val _items = mutableStateOf(ArrayDeque<Item>(), neverEqualPolicy())
    val items: State<List<Item>> = _items

    private var _id = 0L

    @Volatile
    private var _scope: CoroutineScope? = null

    override fun log(level: Level, domain: String?, tag: String, msg: String?, t: Throwable?): Boolean {
        val event = RuntimeEvent.valueOfOrNull(tag) ?: return false
        val scope = _scope ?: return false

        val data = msg
            // RuntimeEvent.ERROR dispatches a Throwable, so msg will be null in that event.
            ?: t?.stackTraceToString()
            ?: return false

        // Should never be the case, but just in case.
        if (data.isEmpty()) return false

        val job = scope.launch {
            val id = _id++
            val items = _items.value
            if (items.size > MAX_ITEMS) items.removeFirst()
            items.add(Item(id, event, data))
            _items.value = items
        }
        return job.isActive
    }

    override fun isLoggable(level: Level, domain: String?, tag: String): Boolean {
        if (domain != DOMAIN) return false
        return _scope != null
    }

    override fun onInstall() {
        val scope = CoroutineScope(context =
            CoroutineName("UILog")
            + SupervisorJob()
            + main
        )
        scope.coroutineContext.job.invokeOnCompletion {
            val i = _items.value
            i.clear()
            _items.value = i
            _id = 0L
        }
        _scope = scope
    }

    override fun onUninstall() {
        val scope = _scope ?: return
        _scope = null
        scope.cancel()
    }
}
