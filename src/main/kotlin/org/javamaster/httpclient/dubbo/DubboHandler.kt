package org.javamaster.httpclient.dubbo

import java.util.concurrent.CompletableFuture

interface DubboHandler {

    fun sendAsync(): CompletableFuture<Triple<ByteArray, String, Long>>

}
