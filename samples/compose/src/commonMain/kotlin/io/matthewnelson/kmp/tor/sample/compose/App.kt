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
@file:Suppress("DEPRECATION")

package io.matthewnelson.kmp.tor.sample.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.matthewnelson.kmp.log.Log
import io.matthewnelson.kmp.tor.runtime.Action
import io.matthewnelson.kmp.tor.runtime.RuntimeEvent
import io.matthewnelson.kmp.tor.runtime.core.OnFailure
import io.matthewnelson.kmp.tor.runtime.core.OnSuccess
import org.jetbrains.compose.ui.tooling.preview.Preview

// All button actions use TorRuntime.enqueue callback API instead of
// the Sync or Async extension functions. It is OK to throw here because
// TorRuntime has a static observer defined for RuntimeEvent.ERROR that
// will pipe the resulting UncaughtException to the UI.
private val ThrowOnFailure = OnFailure { throw it }

/**
 * Compose UI for displaying [RuntimeEvent] logs as a list.
 *
 * @throws [IllegalStateException] If [UILog] is not installed at [Log.Root].
 * */
@Preview
@Composable
fun App() {
    val uiLog = (Log.Root[UILog.UID] as? UILog) ?: throw IllegalStateException("${UILog.UID} must be installed")

    MaterialTheme(colors = darkColors()) {
        Surface {
            var showContent by remember { mutableStateOf(false) }
            val listState = rememberLazyListState()

            Box(
                modifier = Modifier.fillMaxSize().safeContentPadding().padding(8.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                AnimatedVisibility(showContent, Modifier.fillMaxHeight()) {
                    val logItems by remember { uiLog.items }

                    LazyColumn(
                        modifier = Modifier.padding(bottom = 48.dp),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(count = logItems.size, key = { logItems[it].id }) { index ->
                            val item = logItems[index]
                            LogCardItem(item)
                        }
                    }
                }

                if (!showContent) {
                    Button(
                        onClick = {
                            Tor.enqueue(
                                action = Action.StartDaemon,
                                onFailure = ThrowOnFailure,
                                onSuccess = OnSuccess.noOp(),
                            )

                            showContent = !showContent
                        }
                    ) {
                        Text("Start Tor")
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Button(
                            onClick = {
                                Tor.enqueue(
                                    action = Action.StopDaemon,
                                    onFailure = ThrowOnFailure,
                                    onSuccess = OnSuccess.noOp(),
                                )
                            }
                        ) {
                            Text("Stop Tor")
                        }

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = {
                                Tor.enqueue(
                                    action = Action.StartDaemon,
                                    onFailure = ThrowOnFailure,
                                    onSuccess = OnSuccess.noOp(),
                                )
                            }
                        ) {
                            Text("Start Tor")
                        }

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = {
                                Tor.enqueue(
                                    action = Action.RestartDaemon,
                                    onFailure = ThrowOnFailure,
                                    onSuccess = OnSuccess.noOp(),
                                )
                            }
                        ) {
                            Text("Restart Tor")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogCardItem(item: UILog.Item?) {
    var textColor = Color.White

    val bg = when (item?.event) {
        is RuntimeEvent.ERROR -> Color.Red
        is RuntimeEvent.LOG.DEBUG -> {
            var color = Color.Blue
            if (item.data.startsWith("RealTorCtrl")) {
                color = color.copy(alpha = 0.5f)
            }
            color
        }
        is RuntimeEvent.LOG.INFO -> {
            textColor = Color.DarkGray
            Color.Yellow
        }
        is RuntimeEvent.LOG.WARN -> Color.Red.copy(alpha = 0.75f)
        is RuntimeEvent.READY -> {
            textColor = Color.DarkGray
            Color.Green
        }
        else -> Color.DarkGray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = bg,
        elevation = 8.dp
    ) {
        Text(
            modifier = Modifier.padding(4.dp),
            text = item?.data ?: "",
            fontSize = 12.sp,
            color = textColor,
        )
    }
}
