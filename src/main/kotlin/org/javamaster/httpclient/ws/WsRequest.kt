package org.javamaster.httpclient.ws

import com.intellij.codeInspection.ui.actions.LOG
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import org.apache.commons.lang3.exception.ExceptionUtils
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.nio.ByteBuffer
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.function.Consumer

/**
 * 支持 WebSocket请求
 *
 * @author yudong
 */
class WsRequest(
    val url: String,
    private val reqHeaderMap: MutableMap<String, String>,
    val project: Project,
    parentDisposer: Disposable,
) : Disposable {
    private var webSocket: WebSocket? = null
    lateinit var consumer: Consumer<String>

    init {
        Disposer.register(this, parentDisposer)
    }

    fun connect() {
        returnResMsg("正在连接:${url}\r\n")

        val uri = URI(url)

        val client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .build()

        val builder = client.newWebSocketBuilder()
        reqHeaderMap.forEach { builder.header(it.key, it.value) }

        val listener = WsListener(this)

        builder.buildAsync(uri, listener)
            .whenComplete { ws, ex ->
                if (ex != null) {
                    returnResMsg("连接异常:" + ExceptionUtils.getStackTrace(ex) + "\r\n")
                    return@whenComplete
                }
                webSocket = ws
            }
    }

    fun abortConnect() {
        webSocket?.abort()
        LOG.warn("abort ws连接\r\n")
        returnResMsg("中断ws连接:$webSocket\r\n")
    }

    fun sendWsMsg(msg: String) {
        webSocket?.sendText(msg, true)?.whenComplete { _, u ->
            if (u == null) {
                returnResMsg("ws消息发送成功:$msg\r\n")
            } else {
                returnResMsg("ws消息发送失败:${u.message}\r\n")
            }
        }
    }

    fun returnResMsg(msg: String) {
        runInEdt {
            runWriteActionAndWait {
                consumer.accept(msg)
            }
        }
    }

    override fun dispose() {
        abortConnect()
    }
}

class WsListener(private val wsRequest: WsRequest) : WebSocket.Listener {
    override fun onText(webSocket: WebSocket, data: CharSequence?, last: Boolean): CompletionStage<*> {
        webSocket.request(1)

        LOG.warn("收到ws消息:$data")
        wsRequest.returnResMsg("收到ws文本数据:$data\r\n")
        return CompletableFuture<Void>()
    }

    override fun onBinary(webSocket: WebSocket, data: ByteBuffer?, last: Boolean): CompletionStage<*> {
        webSocket.request(1)

        LOG.warn("收到ws二进制消息:$data")
        wsRequest.returnResMsg("收到ws二进制数据:$data\r\n")
        return CompletableFuture<Void>()
    }

    override fun onOpen(webSocket: WebSocket) {
        webSocket.request(1)

        LOG.warn("打开ws连接:$webSocket")
        wsRequest.returnResMsg("ws连接成功:$webSocket\r\n")
    }

    override fun onClose(webSocket: WebSocket, statusCode: Int, reason: String?): CompletionStage<*> {
        LOG.warn("ws连接closed:$webSocket,$statusCode,$reason")
        wsRequest.returnResMsg("ws连接已关闭:$webSocket,statusCode:$statusCode,reason:$reason\r\n")
        return CompletableFuture<Void>()
    }

    override fun onError(webSocket: WebSocket?, error: Throwable?) {
        LOG.warn("连接ws异常:$webSocket", error)
        wsRequest.returnResMsg("连接ws异常:$webSocket,${error?.message}\r\n")
    }
}
