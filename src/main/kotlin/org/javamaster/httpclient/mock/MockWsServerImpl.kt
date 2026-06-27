package org.javamaster.httpclient.mock

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.channel.MultiThreadIoEventLoopGroup
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.mock.support.HttpWebSocketHandler
import org.javamaster.httpclient.mock.support.MockWsServer
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.ui.HttpDashboardForm


/**
 * @author yudong
 */
@Suppress("unused")
class MockWsServerImpl(
    private val port: Int,
    private val path: String,
    reqHeaderMap: LinkedMultiValueMap<String, String?>,
    private val httpDashboardForm: HttpDashboardForm,
) : MockWsServer {
    var channelFuture: ChannelFuture? = null
    var bossGroup: MultiThreadIoEventLoopGroup? = null
    var workerGroup: MultiThreadIoEventLoopGroup? = null

    override fun startServer(request: HttpRequest, variableResolver: VariableResolver, paramMap: Map<String, String>) {
        // 1. 创建线程组 bossGroup 负责接受客户端的连接请求
        bossGroup = MultiThreadIoEventLoopGroup(NioIoHandler.newFactory())

        // workerGroup 负责处理已建立连接的数据读写和业务逻辑
        workerGroup = MultiThreadIoEventLoopGroup(NioIoHandler.newFactory())

        val serverBootstrap = ServerBootstrap()

        serverBootstrap.group(bossGroup, workerGroup)
            // 3. 指定使用 NIO 传输
            .channel(NioServerSocketChannel::class.java)
            .childHandler(object : ChannelInitializer<SocketChannel>() {
                // 4. 设置子处理器
                override fun initChannel(ch: SocketChannel) {
                    val pipeline: ChannelPipeline = ch.pipeline()
                    // 5. 添加编解码器
                    // 将 HTTP 请求和响应进行编解码
                    pipeline.addLast(HttpServerCodec())
                    // 将 HTTP 消息的多个部分聚合为一个完整的 FullHttpRequest 或 FullHttpResponse
                    pipeline.addLast(HttpObjectAggregator(65536))
                    // 6. 添加 WebSocket 协议处理器
                    // 负责 WebSocket 的握手、协议升级以及数据帧的处理
                    pipeline.addLast(WebSocketServerProtocolHandler(path))
                    // 7. 添加自定义的业务处理器
                    pipeline.addLast(HttpWebSocketHandler(httpDashboardForm, request, variableResolver, paramMap))
                }
            })

        channelFuture = serverBootstrap.bind(port).sync()

        httpDashboardForm.showMockServerLog(NlsBundle.nls("mock.ws.server.start", port) + "\n")
    }

    override fun stopServer() {
        channelFuture?.channel()?.close()
        bossGroup?.shutdownGracefully()
        workerGroup?.shutdownGracefully()

        httpDashboardForm.showMockServerLog("WebSocket Server stopped\n")
    }

}