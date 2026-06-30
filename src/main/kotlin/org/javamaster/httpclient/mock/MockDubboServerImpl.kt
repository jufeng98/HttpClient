package org.javamaster.httpclient.mock

import com.alibaba.dubbo.config.ProtocolConfig
import com.alibaba.dubbo.config.RegistryConfig
import com.alibaba.dubbo.config.ServiceConfig
import com.alibaba.dubbo.rpc.service.GenericService
import org.javamaster.httpclient.consts.HttpConsts.Companion.TIMEOUT
import org.javamaster.httpclient.dubbo.DubboRequestImpl
import org.javamaster.httpclient.dubbo.loader.ApiClassLoader
import org.javamaster.httpclient.dubbo.support.DubboBridge
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.mock.support.DubboGenericService
import org.javamaster.httpclient.mock.support.MockDubboServer
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.utils.DubboUtils
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.util.concurrent.CompletableFuture


/**
 * @author yudong
 */
@Suppress("unused")
class MockDubboServerImpl(
    private val port: Int,
    private val schema: String?,
    reqHeaderMap: LinkedMultiValueMap<String, String?>,
    private val dubboBridge: DubboBridge,
) : MockDubboServer {
    private val interfaceName: String by lazy {
        val values = reqHeaderMap[DubboUtils.INTERFACE_NAME] ?: throw IllegalArgumentException(
            NlsBundle.nls(
                "dubbo.all.blank", DubboUtils.INTERFACE_KEY, DubboUtils.INTERFACE_NAME
            )
        )

        val name = values[0]
        if (name.isNullOrBlank()) {
            throw IllegalArgumentException(
                NlsBundle.nls(
                    "dubbo.all.blank", DubboUtils.INTERFACE_KEY, DubboUtils.INTERFACE_NAME
                )
            )
        }

        name
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

    override fun startServerAsync(
        request: HttpRequest,
        variableResolver: VariableResolver,
        paramMap: Map<String, String>,
    ): CompletableFuture<Void> {
        var apiClz: Class<*>? = null
        var apiClassLoader: ApiClassLoader? = null

        val importPath = paramMap[ParamEnum.IMPORT.param]
        if (importPath != null) {
            val jarUrls = mutableListOf<URL>()
            val file = File(importPath)
            if (!file.exists()) {
                throw FileNotFoundException(importPath)
            }

            jarUrls.add(file.toURI().toURL())
            apiClassLoader = ApiClassLoader(jarUrls.toTypedArray(), Thread.currentThread().contextClassLoader)
            apiClz = apiClassLoader.loadClass(interfaceName)
        }

        val serviceConfig = ServiceConfig<GenericService>()
        serviceConfig.application = DubboRequestImpl.application
        serviceConfig.generic = "true"
        serviceConfig.timeout = TIMEOUT
        serviceConfig.retries = 0

        val dubboGenericService = DubboGenericService(
            dubboBridge, request, variableResolver, paramMap, apiClz, apiClassLoader
        )
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

        if (apiClz != null) {
            serviceConfig.setInterface(apiClz)
        } else {
            serviceConfig.`interface` = interfaceName
        }

        if (registry.isNullOrBlank()) {
            registryConfig.address = "N/A"
            registryConfig.isRegister = false
        } else {
            registryConfig.address = registry
            registryConfig.isRegister = true
        }

        serviceConfig.registry = registryConfig

        dubboBridge.showMockServerLog("Dubbo Server starting...\n")

        val classLoaderTmp = apiClassLoader ?: Thread.currentThread().contextClassLoader

        return CompletableFuture.runAsync {
            val classLoader = Thread.currentThread().contextClassLoader

            try {
                Thread.currentThread().contextClassLoader = classLoaderTmp

                serviceConfig.export()

                this.serviceConfig = serviceConfig

                dubboBridge.showMockServerLog(NlsBundle.nls("mock.dubbo.server.start", port) + "\n")
            } finally {
                Thread.currentThread().contextClassLoader = classLoader
            }
        }
    }

    override fun stopServer() {
        serviceConfig?.unexport()

        dubboBridge.showMockServerLog("Dubbo Server stopped\n")
    }

}