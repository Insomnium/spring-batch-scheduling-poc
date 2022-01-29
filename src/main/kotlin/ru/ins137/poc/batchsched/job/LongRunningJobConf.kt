package ru.ins137.poc.batchsched.job

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
import java.time.LocalDateTime
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

        private val key get() = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString()

        @Scheduled(cron = "*/5 * * * * *")
        @Async("longRunningJobThreadPool")
        fun schedule() {
            jobLauncher.run(
                longRunningJob, JobParameters(
                    mapOf(
                        "id" to JobParameter(key, true)
                    )
                )
            )
        }

    }
}

