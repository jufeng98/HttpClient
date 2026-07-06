package org.javamaster.httpclient.utils

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

object ExecutorUtils {
    val scheduledExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
}
