package ru.ins137.poc.batchsched.job

import org.quartz.*
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameter
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean
import org.springframework.scheduling.quartz.QuartzJobBean
import ru.ins137.poc.batchsched.conf.props.AppProps
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
        private val appProps: AppProps
    ) {

        @Bean
        fun quartzJobDetails(): JobDetail = JobBuilder.newJob(LongRunningQuartzJob::class.java)
            .withIdentity("longRunningJob")
            .storeDurably()
            .build()

        @Bean
        fun quartzTrigger(): Trigger = TriggerBuilder.newTrigger()
            .forJob(quartzJobDetails())
            .withIdentity("longRunningJob")
            .forJob("longRunningJob")
            .withSchedule(CronScheduleBuilder.cronSchedule(appProps.jobCron))
            .build()

        @DisallowConcurrentExecution
        @PersistJobDataAfterExecution
        class LongRunningQuartzJob(
            private val jobLauncher: JobLauncher,
            private val longRunningJob: Job
        ) : QuartzJobBean() {

            private val key get() = unique()

            private fun unique(): String = LocalDateTime.now().toString()

            private fun dayConstant(): String = LocalDate.now().toString()

            private fun truncatedToSeconds(): String = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString()

            private fun truncatedToTenSecond(): String = (Math.floor(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC).toDouble() / 10) * 10).toLong().toString()

            override fun executeInternal(context: JobExecutionContext) {
                kotlin.runCatching {
                    jobLauncher.run(
                        longRunningJob, JobParameters(
                            mapOf(
                                "id" to JobParameter(key, true)
                            )
                        )
                    )
                }.onFailure { logger.error(it.javaClass.simpleName + ": " + it.message) }
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LongRunningJobConf::class.java)
    }
}

