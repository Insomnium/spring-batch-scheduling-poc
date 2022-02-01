package ru.ins137.poc.batchsched.conf

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import ru.ins137.poc.batchsched.conf.props.AppProps
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.sql.DataSource

@Configuration
@EnableConfigurationProperties(AppProps::class)
@EnableBatchProcessing
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "1h")
@EnableAsync
class AppConf(
    private val appProps: AppProps
) {

    @Bean
    fun longRunningJobThreadPool(): ExecutorService = Executors.newSingleThreadExecutor()

    @Bean
    fun shedlockLockProvider(ds: DataSource): LockProvider = JdbcTemplateLockProvider(
        JdbcTemplateLockProvider.Configuration.builder()
            .withJdbcTemplate(JdbcTemplate(ds))
            .withLockedByValue(appProps.lock.owner)
            .usingDbTime()
            .build()
    )

}
