package com.purestation.app.annotation

object ServerConfig {
    var protocol: String = "http"
    @JvmField var host: String = "localhost"
    const val PORT: Int = 8080      // 이미 public static final
}
