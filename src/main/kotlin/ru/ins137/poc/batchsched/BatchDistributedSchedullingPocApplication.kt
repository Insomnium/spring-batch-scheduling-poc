package ru.ins137.poc.batchsched

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BatchDistributedSchedullingPocApplication

fun main(args: Array<String>) {
    runApplication<BatchDistributedSchedullingPocApplication>(*args)
}
