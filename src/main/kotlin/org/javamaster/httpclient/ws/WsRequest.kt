package org.javamaster.httpclient.ws

import org.apache.commons.lang3.exception.ExceptionUtils
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.processHandler.ProcessHandlerBase
import org.javamaster.httpclient.ui.HttpDashboardForm
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.nio.ByteBuffer
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

/**
 * Support WebSocket request
 *
 * @author yudong
 */
class WsRequest(
    private val url: String,
    private val reqHeaderMap: LinkedMultiValueMap<String, String?>,
    private val processHandler: ProcessHandlerBase,
    private val paramMap: Map<String, String>,
    private val wsDashboardForm: HttpDashboardForm.WsDashboardForm
) {
    init {
        wsDashboardForm.setWsRequest(this)
    }

    private var webSocket: WebSocket? = null
    private val tabName = processHandler.tabName

    fun connectAsync() {
        wsRunningSet.add(tabName)

        returnResMsg(NlsBundle.nls("connecting") + " ${url}\n")

        val uri = URI(url)

        val connectTimeout = paramMap[ParamEnum.CONNECT_TIMEOUT_NAME.param]?.toLong() ?: HttpConsts.CONNECT_TIMEOUT

        val client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(connectTimeout))
            .build()

        val builder = client.newWebSocketBuilder()
        reqHeaderMap.forEach {
            it.value.forEach { value ->
                builder.header(it.key, value ?: "null")
            }
        }

        val listener = WsListener(this, processHandler)

        builder.buildAsync(uri, listener)
            .whenComplete { ws, ex ->
                if (ex == null) {
                    webSocket = ws
                    return@whenComplete
                }

                wsRunningSet.remove(tabName)

                processHandler.hasReqError = true
                processHandler.detachProcess()

                returnResMsg(NlsBundle.nls("connected.failed") + ExceptionUtils.getStackTrace(ex) + "\n")
            }
    }

    fun abortConnect() {
        wsRunningSet.remove(tabName)

        if (webSocket == null) {
            return
        }

        returnResMsg(NlsBundle.nls("ws.disconnected") + "\n")

        webSocket!!.abort()
    }

    fun sendWsMsg(msg: String) {
        webSocket?.sendText(msg, true)
            ?.whenComplete { _, u ->
                if (u == null) {
                    returnResMsg("↑↑↑ ${NlsBundle.nls("succeed")}:$msg\n")
                } else {
                    returnResMsg("↑↑↑ ${NlsBundle.nls("failed")}:${u.message}\n")
                }
            }
    }

    fun returnResMsg(msg: String) {
        wsDashboardForm.showReceiveWsMsg(msg)
    }

    companion object {
        private val wsRunningSet = mutableSetOf<String>()

        fun isRunning(tabName: String): Boolean {
            return wsRunningSet.contains(tabName)
        }
    }
}

class WsListener(private val wsRequest: WsRequest, private val processHandler: ProcessHandlerBase) :
    WebSocket.Listener {
    override fun onText(webSocket: WebSocket?, data: CharSequence?, last: Boolean): CompletionStage<*> {
        webSocket?.request(1)

        wsRequest.returnResMsg("↓↓↓ ${NlsBundle.nls("text.data")}:$data\n")

        return CompletableFuture<Void>()
    }

    override fun onBinary(webSocket: WebSocket?, data: ByteBuffer?, last: Boolean): CompletionStage<*> {
        webSocket?.request(1)

        wsRequest.returnResMsg("↓↓↓ ${NlsBundle.nls("binary.data")}:$data\n")

        return CompletableFuture<Void>()
    }

    override fun onOpen(webSocket: WebSocket?) {
        webSocket?.request(1)

        wsRequest.returnResMsg("${NlsBundle.nls("connect.succeed")}\n")
    }

    override fun onClose(webSocket: WebSocket?, statusCode: Int, reason: String?): CompletionStage<*> {
        wsRequest.returnResMsg("${NlsBundle.nls("ws.closed")},statusCode: $statusCode, reason: $reason\n")

        processHandler.detachProcess()

        return CompletableFuture<Void>()
    }

    override fun onError(webSocket: WebSocket?, error: Throwable?) {
        wsRequest.returnResMsg("${NlsBundle.nls("ws.failed")}, ${error}\n")

        processHandler.detachProcess()
    }
}
