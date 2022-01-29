package ru.ins137.poc.batchsched.service

import org.springframework.stereotype.Service
import ru.ins137.poc.batchsched.conf.props.AppProps
import java.util.concurrent.atomic.AtomicLong

@Service
class LongRunningService(
    private val appProps: AppProps
) {

    private val counter = AtomicLong(0L)

    fun process(): Long {
        Thread.sleep(appProps.processingDelay.toMillis())
        return counter.incrementAndGet()
    }
}