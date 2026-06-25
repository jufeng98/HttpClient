package org.javamaster.httpclient.dubbo.support

import java.util.concurrent.CompletableFuture

/**
 * @author yudong
 */
interface DubboRequest {

    fun sendAsync(): CompletableFuture<Triple<ByteArray, String, Long>?>

}