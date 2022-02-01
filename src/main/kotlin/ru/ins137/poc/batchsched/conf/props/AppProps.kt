package ru.ins137.poc.batchsched.conf.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.time.Duration

@ConstructorBinding
@ConfigurationProperties("app")
class AppProps(
    val processingDelay: Duration,
    val lock: Lock
)

@ConstructorBinding
data class Lock(
    val owner: String
)
