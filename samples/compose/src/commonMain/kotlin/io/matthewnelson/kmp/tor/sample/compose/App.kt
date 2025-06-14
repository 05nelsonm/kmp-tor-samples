package io.matthewnelson.kmp.tor.sample.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.matthewnelson.kmp.tor.runtime.Action
import io.matthewnelson.kmp.tor.runtime.RuntimeEvent
import io.matthewnelson.kmp.tor.runtime.core.OnFailure
import io.matthewnelson.kmp.tor.runtime.core.OnSuccess
import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.compose.ui.tooling.preview.Preview

// All button actions use TorRuntime.enqueue callback API instead of
// the Sync or Async extension functions. It is OK to throw here because
// TorRuntime has a static observer defined for RuntimeEvent.ERROR that
// will pipe the resulting UncaughtException to the UI.
private val ThrowOnFailure = OnFailure { throw it }

// Jvm desktop does not use Dispatchers.Main, so need to pass in
// MainUiDispatcher from compose so it can be utilized everywhere.
internal expect val UI_DISPATCHER: CoroutineDispatcher?

@Composable
@Preview
fun App() {
    MaterialTheme(colors = darkColors()) {
        Surface {
            var showContent by remember { mutableStateOf(false) }
            val listState = rememberLazyListState()

            Box(
                modifier = Modifier.fillMaxSize().safeContentPadding().padding(8.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                AnimatedVisibility(showContent, Modifier.fillMaxHeight()) {
                    val logItems by remember { LogItem.Holder.getOrCreate(LOG_HOLDER_NAME).items }

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
private fun LogCardItem(item: LogItem?) {
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
