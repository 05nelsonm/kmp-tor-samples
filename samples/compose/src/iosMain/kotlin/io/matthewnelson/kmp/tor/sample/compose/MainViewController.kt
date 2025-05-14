package io.matthewnelson.kmp.tor.sample.compose

import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.CoroutineDispatcher

internal actual val UI_DISPATCHER: CoroutineDispatcher? = null

fun MainViewController() = ComposeUIViewController { App() }
