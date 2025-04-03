package org.javamaster.httpclient.dubbo

import com.alibaba.dubbo.config.ApplicationConfig
import com.alibaba.dubbo.config.ReferenceConfig
import com.alibaba.dubbo.config.RegistryConfig
import com.alibaba.dubbo.rpc.service.GenericService
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.utils.DubboUtils
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.TIMEOUT_NAME
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
) {
    private val methodName: String by lazy {
        val values = reqHeaderMap[DubboUtils.METHOD_KEY] ?: throw IllegalArgumentException("Missing Method header!")
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
    private val paramTypeNameArray: Array<String?>
    private val paramValueArray: Array<Any?>

    init {
        if (interfaceCls != null) {
            targetInterfaceName = interfaceCls!!
            val psiClass = if (module != null) {
                DubboUtils.findInterface(module, targetInterfaceName)
                    ?: throw IllegalArgumentException("Can't resolve interface: $targetInterfaceName in module ${module.name} !")
            } else {
                DubboUtils.findInterface(project, targetInterfaceName)
                    ?: throw IllegalArgumentException("Can't resolve interface: $targetInterfaceName in project ${project.name} !")
            }

            val targetMethod = findTargetMethod(psiClass, reqBodyMap)

            paramTypeNameArray = targetMethod.parameterList.parameters
                .map {
                    val psiType = PsiUtils.resolvePsiType(it.type)!!
                    psiType.qualifiedName
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
                throw IllegalArgumentException("Header ${DubboUtils.INTERFACE_KEY} and ${DubboUtils.INTERFACE_NAME} can't be blank at the same time!")
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
                        ?: throw IllegalArgumentException("Missing $paramName header!")
                    tmpTypeList.add(argTypes[0])
                    tmpValueList.add(entry.value)
                }

                paramTypeNameArray = tmpTypeList.toTypedArray()
                paramValueArray = tmpValueList.toTypedArray()
            }
        }
    }

    @Suppress("unused")
    fun sendAsync(): CompletableFuture<Pair<ByteArray, Long>> {
        val commentTabName = "### $tabName\r\n"
        httpReqDescList.add(commentTabName)
        httpReqDescList.add("DUBBO $url\r\n")

        reqHeaderMap.forEach {
            val name = it.key
            it.value.forEach { value ->
                httpReqDescList.add("$name: $value\r\n")
            }
        }

        httpReqDescList.add("\r\n")
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
                val result: Any
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
            registryConfig.isRegister = false

            reference.registry = registryConfig
        }

        return reference
    }

    private fun findTargetMethod(psiClass: PsiClass, reqMap: LinkedHashMap<*, *>?): PsiMethod {
        val methods = psiClass.findMethodsByName(methodName, false)
        if (methods.isEmpty()) {
            throw IllegalArgumentException("Method $methodName not exist!")
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
                throw IllegalArgumentException("According to the method param $paramNames, can't match any method: ${methodName}!")
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
