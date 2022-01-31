package ru.ins137.poc.batchsched.conf

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.ins137.poc.batchsched.conf.props.AppProps
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Configuration
@EnableConfigurationProperties(AppProps::class)
@EnableBatchProcessing
class AppConf {

    @Bean
    fun longRunningJobThreadPool(): ExecutorService = Executors.newFixedThreadPool(10)
}
