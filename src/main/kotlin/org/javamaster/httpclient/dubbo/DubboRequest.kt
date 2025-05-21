package org.javamaster.httpclient.dubbo

import com.alibaba.dubbo.config.ApplicationConfig
import com.alibaba.dubbo.config.ReferenceConfig
import com.alibaba.dubbo.config.RegistryConfig
import com.alibaba.dubbo.rpc.service.GenericService
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiPrimitiveType
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.utils.DubboUtils
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.PsiUtils
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

/**
 * @author yudong
 */
class DubboRequest(
    private val tabName: String,
    private val url: String,
    private val reqHeaderMap: LinkedMultiValueMap<String, String>,
    private val reqBodyStr: Any?,
    private val httpReqDescList: MutableList<String>,
    module: Module?,
    project: Project,
    private val paramMap: Map<String, String>,
) : DubboHandler {
    private val methodName: String by lazy {
        val values =
            reqHeaderMap[DubboUtils.METHOD_KEY] ?: throw IllegalArgumentException(NlsBundle.nls("missing.header"))
        values[0]
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
    private val registry by lazy {
        val values = reqHeaderMap[DubboUtils.REGISTRY] ?: return@lazy null
        values[0]
    }
    private val reqBodyMap: LinkedHashMap<*, *>? = if (reqBodyStr != null) {
        HttpUtils.gson.fromJson(reqBodyStr as String, LinkedHashMap::class.java)
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

            val targetMethod = findTargetMethod(psiClass, reqBodyMap)

            paramTypeNameArray = targetMethod.parameterList.parameters
                .map {
                    val type = it.type
                    val psiType = PsiUtils.resolvePsiType(type)
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

            paramValueArray = targetMethod.parameterList.parameters
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
                    tmpTypeList.add(argTypes[0])
                    tmpValueList.add(entry.value)
                }

                paramTypeNameArray = tmpTypeList.toTypedArray()
                paramValueArray = tmpValueList.toTypedArray()
            }
        }
    }

    override fun sendAsync(): CompletableFuture<Pair<ByteArray, Long>> {
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
        httpReqDescList.add("DUBBO $url$CR_LF")

        reqHeaderMap.forEach {
            val name = it.key
            it.value.forEach { value ->
                httpReqDescList.add("$name: $value$CR_LF")
            }
        }

        httpReqDescList.add(CR_LF)

        if (reqBodyMap != null) {
            httpReqDescList.add(reqBodyStr as String)
        }

        return CompletableFuture.supplyAsync {
            val classLoader = Thread.currentThread().contextClassLoader
            try {
                Thread.currentThread().contextClassLoader = javaClass.classLoader

                val referenceConfig = createReferenceConfig()

                val genericService = referenceConfig.get()

                val start = System.currentTimeMillis()
                val result: Any?
                try {
                    result = genericService.`$invoke`(methodName, paramTypeNameArray, paramValueArray)
                } finally {
                    referenceConfig.destroy()
                }
                val consumeTimes = System.currentTimeMillis() - start

                val resJsonStr = HttpUtils.gson.toJson(result)

                val byteArray = resJsonStr.toByteArray(StandardCharsets.UTF_8)

                Pair(byteArray, consumeTimes)
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
        val timeout = paramMap[ParamEnum.TIMEOUT_NAME.param]?.toInt() ?: HttpUtils.TIMEOUT
        reference.timeout = timeout
        reference.retries = 1
        reference.isCheck = false

        if (!version.isNullOrBlank()) {
            reference.version = version
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

    private fun findTargetMethod(psiClass: PsiClass, reqMap: LinkedHashMap<*, *>?): PsiMethod {
        val methods = psiClass.findMethodsByName(methodName, false)
        if (methods.isEmpty()) {
            throw IllegalArgumentException(NlsBundle.nls("method.not.exists", methodName))
        }

        val method: PsiMethod?
        if (reqMap == null) {
            method = methods[0]
        } else {
            val paramNames = reqMap.keys
            method = methods.filter {
                val iterator = paramNames.iterator()
                val parameterList = it.parameterList
                for (i in 0 until parameterList.parametersCount) {
                    val name = parameterList.parameters[i].name
                    val jsonName = iterator.next()
                    if (name != jsonName) {
                        return@filter false
                    }
                }
                true
            }.firstOrNull()

            if (method == null) {
                throw IllegalArgumentException(NlsBundle.nls("method.not.found", paramNames, methodName))
            }
        }

        return method!!
    }

    companion object {
        private val application = ApplicationConfig()

        init {
            application.name = "HttpClient"
            application.qosEnable = false
        }

    }
}
