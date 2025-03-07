package org.javamaster.httpclient.dubbo

import com.alibaba.dubbo.config.ApplicationConfig
import com.alibaba.dubbo.config.ReferenceConfig
import com.alibaba.dubbo.config.RegistryConfig
import com.alibaba.dubbo.rpc.service.GenericService
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import org.javamaster.httpclient.utils.DubboUtils
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.TIMEOUT_NAME
import org.javamaster.httpclient.utils.PsiUtils
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture


class DubboRequest(
    private val tabName: String,
    private val url: String,
    private val reqHeaderMap: MutableMap<String, String>,
    private val reqBodyStr: Any?,
    private val httpReqDescList: MutableList<String>,
    module: Module,
    private val paramMap: Map<String, String>,
) {
    private val methodName: String = reqHeaderMap[DubboUtils.METHOD_KEY]!!
    private val interfaceCls: String? = reqHeaderMap[DubboUtils.INTERFACE_KEY]
    private val interfaceName: String? = reqHeaderMap["Interface-Name"]
    private val version = reqHeaderMap["Version"]
    private val registry = reqHeaderMap["Registry"]
    private val reqMap: LinkedHashMap<*, *>? = if (reqBodyStr != null) {
        HttpUtils.gson.fromJson(reqBodyStr as String, LinkedHashMap::class.java)
    } else {
        null
    }

    private val targetInterfaceName: String
    private val paramTypeNameArray: Array<String?>
    private val paramValueArray: Array<Any?>

    init {
        if (interfaceCls != null) {
            targetInterfaceName = interfaceCls
            val psiClass = DubboUtils.findInterface(module, interfaceCls)
                ?: throw IllegalArgumentException("无法解析接口:${interfaceCls}!")

            val targetMethod = findTargetMethod(psiClass, reqMap)

            paramTypeNameArray = targetMethod.parameterList.parameters
                .map {
                    val psiType = PsiUtils.resolvePsiType(it.type)!!
                    psiType.qualifiedName
                }
                .toTypedArray()

            paramValueArray = targetMethod.parameterList.parameters
                .map {
                    val name = it.name
                    reqMap!![name]
                }
                .toTypedArray()
        } else {
            if (interfaceName == null) {
                throw IllegalArgumentException(DubboUtils.INTERFACE_KEY + " 和 Interface-Name 不能都为空")
            }

            targetInterfaceName = interfaceName
            if (reqMap == null) {
                paramTypeNameArray = arrayOf()
                paramValueArray = arrayOf()
            } else {
                val paramNames = reqMap.entries

                val tmpTypeList = mutableListOf<String>()
                val tmpValueList = mutableListOf<Any>()

                for (entry in paramNames) {
                    val headerName = "${entry.key}-Type"
                    val argType = reqHeaderMap[headerName] ?: throw IllegalArgumentException("缺少${headerName}请求头")
                    tmpTypeList.add(argType)
                    tmpValueList.add(entry.value)
                }

                paramTypeNameArray = tmpTypeList.toTypedArray()
                paramValueArray = tmpValueList.toTypedArray()
            }
        }
    }

    fun sendAsync(): CompletableFuture<Pair<ByteArray, Long>> {
        val reference = ReferenceConfig<GenericService>()
        reference.isGeneric = true
        reference.application = application
        reference.setInterface(targetInterfaceName)
        val timeout = paramMap[TIMEOUT_NAME]?.toInt() ?: 10_000
        reference.timeout = timeout
        reference.retries = 1

        if (!version.isNullOrBlank()) {
            reference.version = version
        }

        if (registry.isNullOrBlank()) {
            reference.url = url
        } else {
            val registryConfig = RegistryConfig()
            registryConfig.address = registry
            registryConfig.timeout = timeout
            reference.registry = registryConfig
        }


        val commentTabName = "### $tabName\r\n"
        httpReqDescList.add(commentTabName)
        httpReqDescList.add("DUBBO $url\r\n")
        reqHeaderMap.forEach {
            httpReqDescList.add(it.key + ": " + it.value + "\r\n")
        }
        httpReqDescList.add("\r\n")
        if (reqMap != null) {
            httpReqDescList.add(reqBodyStr as String)
        }

        return CompletableFuture.supplyAsync {
            val classLoader = Thread.currentThread().contextClassLoader
            try {
                Thread.currentThread().contextClassLoader = javaClass.classLoader

                val genericService = reference.get()

                val start = System.currentTimeMillis()

                val result = genericService.`$invoke`(methodName, paramTypeNameArray, paramValueArray)

                val resJsonStr = HttpUtils.gson.toJson(result)
                val byteArray = resJsonStr.toByteArray(StandardCharsets.UTF_8)

                val consumeTimes = System.currentTimeMillis() - start

                Pair(byteArray, consumeTimes)
            } finally {
                Thread.currentThread().contextClassLoader = classLoader
                reference.destroy()
            }
        }
    }

    private fun findTargetMethod(psiClass: PsiClass, reqMap: LinkedHashMap<*, *>?): PsiMethod {
        val methods = psiClass.findMethodsByName(methodName, false)
        if (methods.isEmpty()) {
            throw IllegalArgumentException("方法${methodName}不存在!")
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
                throw IllegalArgumentException("根据方法参数名${paramNames}无法匹配方法:${methodName}!")
            }
        }

        return method!!
    }

    companion object {
        @JvmStatic
        private val application = ApplicationConfig()

        init {
            application.name = "HttpClient"
        }

    }
}
