package org.javamaster.httpclient.dubbo

import com.alibaba.dubbo.config.ApplicationConfig
import com.alibaba.dubbo.config.ReferenceConfig
import com.alibaba.dubbo.config.RegistryConfig
import com.alibaba.dubbo.rpc.service.GenericService
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiPrimitiveType
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.consts.HttpConsts.Companion.TIMEOUT
import org.javamaster.httpclient.dubbo.support.DubboRequest
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.utils.DubboUtils
import org.javamaster.httpclient.utils.DubboUtils.findTargetMethod
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.JsonUtils.gson
import org.javamaster.httpclient.utils.PsiTypeUtils
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

/**
 * @author yudong
 */
class DubboRequestImpl(
    private val tabName: String,
    private val url: String,
    private val reqHeaderMap: LinkedMultiValueMap<String, String?>,
    reqBody: Any?,
    private val httpReqDescList: MutableList<String>,
    module: Module?,
    project: Project,
    private val paramMap: Map<String, String>,
) : DubboRequest {
    private val methodName: String by lazy {
        val values =
            reqHeaderMap[DubboUtils.METHOD_KEY] ?: throw IllegalArgumentException(NlsBundle.nls("missing.header"))
        values[0] ?: throw IllegalArgumentException(NlsBundle.nls("missing.header"))
    }
    private val interfaceCls: String? by lazy {
        val values = reqHeaderMap[DubboUtils.INTERFACE_KEY] ?: return@lazy null
        values[0]
    }
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
    private val reqBodyStr = if (reqBody != null) {
        @Suppress("UNCHECKED_CAST")
        val triple = reqBody as Triple<ByteArray?, String?, ContentType?>

        triple.second
    } else {
        null
    }
    private val reqBodyMap: LinkedHashMap<*, *>? = if (reqBodyStr != null) {
        gson.fromJson(reqBodyStr, LinkedHashMap::class.java)
    } else {
        null
    }

    private val targetInterfaceName: String
    private val paramTypeNameArray: Array<String>
    private val paramValueArray: Array<Any?>

    init {
        if (interfaceCls != null) {
            targetInterfaceName = interfaceCls!!
            val psiClass = if (module != null) {
                DubboUtils.findInterface(module, targetInterfaceName)
                    ?: throw IllegalArgumentException(
                        NlsBundle.nls(
                            "interface.not.resolved",
                            targetInterfaceName,
                            module.name
                        )
                    )
            } else {
                DubboUtils.findInterface(project, targetInterfaceName)
                    ?: throw IllegalArgumentException(
                        NlsBundle.nls(
                            "interface.not.resolved1",
                            targetInterfaceName,
                            project.name
                        )
                    )
            }

            val targetMethod = findTargetMethod(psiClass, methodName, reqBodyMap)

            val parameters = targetMethod.parameterList.parameters

            paramTypeNameArray = parameters
                .map {
                    val type = it.type
                    val psiType = PsiTypeUtils.resolvePsiType(type)
                    if (psiType != null) {
                        val qualifiedName = psiType.qualifiedName
                        if (qualifiedName != null) {
                            return@map qualifiedName
                        }
                    }

                    if (type is PsiPrimitiveType) {
                        return@map type.name
                    }

                    throw IllegalArgumentException(NlsBundle.nls("param.not.resolved", it.name))
                }
                .toTypedArray()

            paramValueArray = parameters
                .map {
                    val name = it.name
                    reqBodyMap!![name]
                }
                .toTypedArray()
        } else {
            if (interfaceName == null) {
                throw IllegalArgumentException(
                    NlsBundle.nls(
                        "dubbo.all.blank",
                        DubboUtils.INTERFACE_KEY,
                        DubboUtils.INTERFACE_NAME
                    )
                )
            }

            targetInterfaceName = interfaceName!!
            if (reqBodyMap == null) {
                paramTypeNameArray = arrayOf()
                paramValueArray = arrayOf()
            } else {
                val paramNames = reqBodyMap.entries

                val tmpTypeList = mutableListOf<String>()
                val tmpValueList = mutableListOf<Any>()

                for (entry in paramNames) {
                    val paramName = "${entry.key}"
                    val argTypes = reqHeaderMap[paramName]
                        ?: throw IllegalArgumentException(NlsBundle.nls("dubbo.miss.header", paramName))
                    tmpTypeList.add(
                        argTypes[0] ?: throw IllegalArgumentException(
                            NlsBundle.nls(
                                "dubbo.miss.header",
                                paramName
                            )
                        )
                    )
                    tmpValueList.add(entry.value)
                }

                paramTypeNameArray = tmpTypeList.toTypedArray()
                paramValueArray = tmpValueList.toTypedArray()
            }
        }
    }

    override fun sendAsync(): CompletableFuture<Triple<ByteArray, String, Long>?> {
        httpReqDescList.add("/*$CR_LF")
        httpReqDescList.add(NlsBundle.nls("call.dubbo.name", methodName) + CR_LF)

        httpReqDescList.add(
            NlsBundle.nls("call.dubbo.param.typeNames", paramTypeNameArray.contentToString()) + CR_LF
        )

        httpReqDescList.add(
            NlsBundle.nls("call.dubbo.params", paramValueArray.contentToString()) + CR_LF
        )
        httpReqDescList.add("*/$CR_LF")

        val commentTabName = "### $tabName$CR_LF"
        httpReqDescList.add(commentTabName)

        if (paramMap.containsKey(ParamEnum.VISUALIZE_TIMESTAMP.param)) {
            httpReqDescList.add("# @${ParamEnum.VISUALIZE_TIMESTAMP.param}$CR_LF")
        }

        httpReqDescList.add("DUBBO $url$CR_LF")

        reqHeaderMap.forEach {
            val name = it.key
            it.value.forEach { value ->
                httpReqDescList.add("$name: $value$CR_LF")
            }
        }

        httpReqDescList.add(CR_LF)

        if (reqBodyStr != null) {
            httpReqDescList.add(reqBodyStr)
        }

        return CompletableFuture.supplyAsync {
            val classLoader = Thread.currentThread().contextClassLoader
            try {
                Thread.currentThread().contextClassLoader = javaClass.classLoader

                val referenceConfig = createReferenceConfig()

                try {
                    val genericService = referenceConfig.get()

                    val start = System.currentTimeMillis()
                    val result: Any? = genericService.`$invoke`(methodName, paramTypeNameArray, paramValueArray)
                    val consumeTimes = System.currentTimeMillis() - start

                    val resJsonStr = gson.toJson(result)

                    val byteArray = resJsonStr.toByteArray(StandardCharsets.UTF_8)

                    Triple(byteArray, resJsonStr, consumeTimes)
                } finally {
                    referenceConfig.destroy()
                }
            } finally {
                Thread.currentThread().contextClassLoader = classLoader
            }
        }
    }

    private fun createReferenceConfig(): ReferenceConfig<GenericService> {
        val reference = ReferenceConfig<GenericService>()
        reference.isGeneric = true
        reference.application = application
        reference.setInterface(targetInterfaceName)

        val timeout = paramMap[ParamEnum.TIMEOUT_NAME.param]?.toInt() ?: TIMEOUT
        reference.timeout = timeout

        reference.retries = 0
        reference.isCheck = false
        reference.reconnect = "false"

        if (!version.isNullOrBlank()) {
            reference.version = version
        }

        if (!group.isNullOrBlank()) {
            reference.group = group
        }

        if (registry.isNullOrBlank()) {
            reference.url = url
        } else {
            val registryConfig = RegistryConfig()
            registryConfig.address = registry
            registryConfig.timeout = timeout
            registryConfig.isRegister = false

            reference.registry = registryConfig
        }

        return reference
    }

    companion object {
        val application = ApplicationConfig()

        init {
            application.name = "HttpRequest"
            application.qosEnable = false
            application.logger = "slf4j"
        }

    }
}
