package org.javamaster.httpclient.mock.support

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.logger.HttpRequestLogger.logWarn
import org.javamaster.httpclient.mock.support.MockServerHelper.appendTime
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.ui.HttpDashboardForm
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * @author yudong
 */
class HttpWebSocketHandler(
    private val httpDashboardForm: HttpDashboardForm,
    private val request: HttpRequest,
    private val variableResolver: VariableResolver,
    private val paramMap: Map<String, String>,
) :
    SimpleChannelInboundHandler<TextWebSocketFrame>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: TextWebSocketFrame) {
        val reqStr = msg.text()
        httpDashboardForm.showMockServerLog("收到消息: $reqStr\n")

        val timeout = paramMap[ParamEnum.TIMEOUT_NAME.param]?.toLong()
        if (timeout != null) {
            httpDashboardForm.showMockServerLog("Sleeping: $timeout ms\n")
            TimeUnit.MILLISECONDS.sleep(timeout)
        }

        val bodyStr = DubboResultGenerator.generate(request, variableResolver)

        val byteArray = bodyStr.toByteArray(StandardCharsets.UTF_8)
        val size = byteArray.size

        httpDashboardForm.showMockServerLog(appendTime(NlsBundle.nls("mock.server.res") + "Content-Length $size b\n"))
        httpDashboardForm.showMockServerLog("-----------------------------\n")

        ctx.channel().writeAndFlush(TextWebSocketFrame(bodyStr))
    }

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        httpDashboardForm.showMockServerLog("有新客户端连接: " + ctx.channel().id() + "\n")
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        httpDashboardForm.showMockServerLog("客户端断开连接: " + ctx.channel().id() + "\n")
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        httpDashboardForm.showMockServerLog("出现异常: $cause\n")
        logWarn("出现异常", cause)
        ctx.close()
    }

}