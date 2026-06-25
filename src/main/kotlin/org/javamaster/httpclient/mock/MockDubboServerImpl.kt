package org.javamaster.httpclient.mock

import com.alibaba.dubbo.config.ProtocolConfig
import com.alibaba.dubbo.config.RegistryConfig
import com.alibaba.dubbo.config.ServiceConfig
import com.alibaba.dubbo.rpc.service.GenericService
import org.javamaster.httpclient.consts.HttpConsts.Companion.TIMEOUT
import org.javamaster.httpclient.dubbo.DubboRequestImpl
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.mock.support.DubboGenericService
import org.javamaster.httpclient.mock.support.MockDubboServer
import org.javamaster.httpclient.mock.support.MockServerHelper
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.utils.DubboUtils
import java.util.function.Consumer


/**
 * @author yudong
 */
@Suppress("unused")
class MockDubboServerImpl(
    private val port: Int,
    private val schema: String?,
    reqHeaderMap: LinkedMultiValueMap<String, String?>,
    private val resConsumer: Consumer<String>,
) : MockDubboServer {
    private val interfaceName: String? by lazy {
        val values = reqHeaderMap[DubboUtils.INTERFACE_NAME] ?: return@lazy null
        values[0]
    }
    private val version by lazy {
        val values = reqHeaderMap[DubboUtils.VERSION] ?: return@lazy null
        values[0]
    }
    private val group by lazy {
        val values = reqHeaderMap[DubboUtils.GROUP] ?: return@lazy null
        values[0]
    }
    private val registry by lazy {
        val values = reqHeaderMap[DubboUtils.REGISTRY] ?: return@lazy null
        values[0]
    }

    private var serviceConfig: ServiceConfig<GenericService>? = null

    override fun startServer(request: HttpRequest, variableResolver: VariableResolver, paramMap: Map<String, String>) {
        val dubboGenericService = DubboGenericService(resConsumer, request, variableResolver, paramMap)

        val serviceConfig = ServiceConfig<GenericService>()
        serviceConfig.application = DubboRequestImpl.application
        serviceConfig.generic = "true"
        serviceConfig.timeout = TIMEOUT
        serviceConfig.retries = 0
        serviceConfig.`interface` = interfaceName
        serviceConfig.ref = dubboGenericService

        val protocol = ProtocolConfig()
        protocol.setName(schema ?: "dubbo")
        protocol.port = port
        serviceConfig.protocol = protocol

        if (version != null) {
            serviceConfig.version = version
        }

        if (group != null) {
            serviceConfig.group = group
        }

        val registryConfig = RegistryConfig()
        registryConfig.timeout = TIMEOUT

        if (registry.isNullOrBlank()) {
            registryConfig.address = "N/A"
            registryConfig.isRegister = false
        } else {
            registryConfig.address = registry
            registryConfig.isRegister = true
        }

        serviceConfig.registry = registryConfig

        serviceConfig.export()

        this.serviceConfig = serviceConfig

        resConsumer.accept(MockServerHelper.appendTime(NlsBundle.nls("mock.server.start", port) + "\n"))
    }

    override fun stopServer() {
        serviceConfig?.unexport()

        resConsumer.accept(MockServerHelper.appendTime("Server stopped\n"))
    }

}