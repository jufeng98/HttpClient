package org.javamaster.httpclient.handler

import java.net.http.HttpResponse
import java.nio.ByteBuffer
import java.util.concurrent.CompletionStage
import java.util.concurrent.Flow
import java.util.function.IntConsumer

object ProgressBodyHandler {

    fun <T> ofProgress(
        interval: Long,
        delegate: HttpResponse.BodyHandler<T>,
        progressCallback: IntConsumer,
    ): HttpResponse.BodyHandler<T> {
        return HttpResponse.BodyHandler {
            object : HttpResponse.BodySubscriber<T> {
                private val delegateSubscriber: HttpResponse.BodySubscriber<T> = delegate.apply(it)

                private var receivedBytes = 0
                private var lastReportedBytes = 0

                override fun onSubscribe(subscription: Flow.Subscription?) {
                    delegateSubscriber.onSubscribe(subscription)
                }

                override fun onNext(items: MutableList<ByteBuffer>) {
                    receivedBytes += items.stream().mapToInt { it.remaining() }.sum()

                    if (receivedBytes - lastReportedBytes >= interval) {
                        progressCallback.accept(receivedBytes)
                        lastReportedBytes = receivedBytes
                    }

                    delegateSubscriber.onNext(items)
                }

                override fun onError(throwable: Throwable) {
                    delegateSubscriber.onError(throwable)
                }

                override fun onComplete() {
                    progressCallback.accept(receivedBytes)
                    delegateSubscriber.onComplete()
                }

                override fun getBody(): CompletionStage<T> {
                    return delegateSubscriber.getBody()
                }
            }
        }

    }

}
