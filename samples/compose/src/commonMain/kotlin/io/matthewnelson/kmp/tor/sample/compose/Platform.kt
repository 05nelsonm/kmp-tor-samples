package io.matthewnelson.kmp.tor.sample.compose

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
