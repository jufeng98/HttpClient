package org.javamaster.httpclient.handler

import org.javamaster.httpclient.function.BitIntConsumer
import java.net.http.HttpResponse
import java.nio.ByteBuffer
import java.util.concurrent.CompletionStage
import java.util.concurrent.Flow

object ProgressBodyHandler {

    fun <T> ofProgress(
        interval: Long,
        total: Int,
        info: HttpResponse.ResponseInfo,
        delegate: HttpResponse.BodyHandler<T>,
        progressCallback: BitIntConsumer,
    ): HttpResponse.BodySubscriber<T> {
        return object : HttpResponse.BodySubscriber<T> {
            private val delegateSubscriber: HttpResponse.BodySubscriber<T> = delegate.apply(info)

            private var receivedBytes = 0
            private var lastReportedBytes = 0

            override fun onSubscribe(subscription: Flow.Subscription?) {
                delegateSubscriber.onSubscribe(subscription)
            }

            override fun onNext(items: MutableList<ByteBuffer>) {
                receivedBytes += items.stream().mapToInt { it.remaining() }.sum()

                val intervalLength = receivedBytes - lastReportedBytes
                if (intervalLength >= interval) {
                    progressCallback.accept(receivedBytes, total)

                    lastReportedBytes = receivedBytes
                }

                delegateSubscriber.onNext(items)
            }

            override fun onError(throwable: Throwable) {
                delegateSubscriber.onError(throwable)
            }

            override fun onComplete() {
                progressCallback.accept(receivedBytes, total)
                delegateSubscriber.onComplete()
            }

            override fun getBody(): CompletionStage<T> {
                return delegateSubscriber.getBody()
            }
        }
    }

}
