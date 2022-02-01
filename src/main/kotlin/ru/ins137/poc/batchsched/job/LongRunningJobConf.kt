package ru.ins137.poc.batchsched.job

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.batch.core.*
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@Configuration
class LongRunningJobConf(
    private val stepBuilderFactory: StepBuilderFactory,
    private val jobBuilderFactory: JobBuilderFactory,
    private val tasklet: Tasklet,
) {

    @Bean
    fun longRunningTaskletStep(): Step = stepBuilderFactory["long-running-tasklet-step"]
        .tasklet(tasklet)
        .allowStartIfComplete(true)
        .build()

    @Bean
    fun longRunningJob(): Job = jobBuilderFactory["long-running-job"]
        .start(longRunningTaskletStep())
        .build()

    @Configuration
    @Order(1)
    class LongRunningJobScheduler(
        private val jobLauncher: JobLauncher,
        private val longRunningJob: Job
    ) {

        private val key get() = unique()

        private fun unique(): String = LocalDateTime.now().toString()

        private fun dayConstant(): String = LocalDate.now().toString()

        private fun truncatedToSeconds(): String = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString()

        private fun truncatedToTenSecond(): String = (Math.floor(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC).toDouble() / 10) * 10).toLong().toString()

        @Scheduled(cron = "\${app.job-cron}")
        @SchedulerLock(name = "longRunningJobLock", lockAtLeastFor = "\${app.lock.least}", lockAtMostFor = "\${app.lock.most}")
        @Async("longRunningJobThreadPool") // has to be single threaded pool, as lock is reentrant
        fun schedule() {
            kotlin.runCatching {
                jobLauncher.run(
                    longRunningJob, JobParameters(
                        mapOf(
                            "id" to JobParameter(key, true)
                        )
                    )
                )
            }.getOrElse { logger.error(it.javaClass.simpleName + ": " + it.message) }
        }

    }

    companion object {
        private val logger = LoggerFactory.getLogger(LongRunningJobConf::class.java)
    }
}

