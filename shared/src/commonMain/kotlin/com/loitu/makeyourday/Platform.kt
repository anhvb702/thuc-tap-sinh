package com.loitu.makeyourday

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform