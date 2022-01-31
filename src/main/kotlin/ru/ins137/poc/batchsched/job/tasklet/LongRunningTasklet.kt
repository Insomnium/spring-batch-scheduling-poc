package ru.ins137.poc.batchsched.job.tasklet

import org.slf4j.LoggerFactory
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.stereotype.Component
import ru.ins137.poc.batchsched.conf.props.AppProps
import ru.ins137.poc.batchsched.service.LongRunningService

@Component
class LongRunningTasklet(
    private val service: LongRunningService,
    private val appProps: AppProps
) : Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        logger.info("Running tasklet: ${service.startProcess()}")
        Thread.sleep(appProps.processingDelay.toMillis())
        service.endProcess()
        return RepeatStatus.FINISHED
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LongRunningTasklet::class.java)
    }
}
