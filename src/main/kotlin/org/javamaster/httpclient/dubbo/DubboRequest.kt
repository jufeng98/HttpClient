package org.javamaster.httpclient.dubbo

import com.intellij.openapi.module.Module
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import org.javamaster.httpclient.dubbo.loader.DubboClassLoader
import org.javamaster.httpclient.dubbo.support.DubboJars
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.utils.DubboUtils
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.TIMEOUT_NAME
import org.javamaster.httpclient.utils.PsiUtils
import java.lang.reflect.Method
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
    module: Module,
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

    private val application by lazy {
        val application = newInstance("com.alibaba.dubbo.config.ApplicationConfig")
        invokeMethod(application, "httpClient", "setName", String::class.java)
        application
    }

    private val targetInterfaceName: String
    private val paramTypeNameArray: Array<String?>
    private val paramValueArray: Array<Any?>

    init {
        if (interfaceCls != null) {
            targetInterfaceName = interfaceCls!!
            val psiClass = DubboUtils.findInterface(module, interfaceCls!!)
                ?: throw IllegalArgumentException("Can't resolve interface: $interfaceCls in module ${module.name} !")

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
                Thread.currentThread().contextClassLoader = dubboClassLoader

                val reference = createReference()

                val genericService = invokeMethod(reference, null, "get")

                val start = System.currentTimeMillis()
                val result: Any?
                try {
                    val genericCls = genericService!!::class.java

                    val method = genericCls.declaredMethods.first { it.name == "\$invoke" }
                    method.isAccessible = true

                    result = method.invoke(genericService, methodName, paramTypeNameArray, paramValueArray)
                } finally {
                    invokeMethod(reference, null, "destroy")
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

    private fun createReference(): Any {
        val reference = newInstance("com.alibaba.dubbo.config.ReferenceConfig")

        invokeMethod(reference, true, "setGeneric", Boolean::class.javaObjectType)
        invokeMethod(reference, application, "setApplication", application::class.java)
        invokeMethod(reference, targetInterfaceName, "setInterface", String::class.java)

        val timeout = paramMap[TIMEOUT_NAME]?.toInt() ?: 10_000
        invokeMethod(reference, timeout, "setTimeout", timeout::class.javaObjectType)
        invokeMethod(reference, 0, "setRetries", Int::class.javaObjectType)

        if (!version.isNullOrBlank()) {
            invokeMethod(reference, version!!, "setVersion", version!!::class.java)
        }

        if (registry.isNullOrBlank()) {
            invokeMethod(reference, url, "setUrl", url::class.java)
        } else {
            val registryConfig = newInstance("com.alibaba.dubbo.config.RegistryConfig")
            invokeMethod(registryConfig, registry!!, "setAddress", registry!!::class.java)
            invokeMethod(registryConfig, false, "setRegister", Boolean::class.javaObjectType)
            invokeMethod(registryConfig, timeout, "setTimeout", timeout::class.javaObjectType)
            invokeMethod(reference, registryConfig, "setRegistry", registryConfig::class.java)
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
                throw IllegalArgumentException("According to the method param $paramNames can't match method: ${methodName}!")
            }
        }

        return method!!
    }

    private fun newInstance(name: String): Any {
        val referenceConfigCls = dubboClassLoader.loadClass(name)
        val declaredConstructor = referenceConfigCls.getDeclaredConstructor()
        declaredConstructor.setAccessible(true)
        return declaredConstructor.newInstance()
    }

    private fun invokeMethod(obj: Any, value: Any?, name: String, vararg paramCls: Class<*>): Any? {
        val clazz = obj.javaClass

        val method = getMethod(clazz, name, *paramCls)

        method.isAccessible = true

        return if (value != null) {
            method.invoke(obj, value)
        } else {
            method.invoke(obj)
        }
    }

    private fun getMethod(clazz: Class<*>, name: String, vararg paramCls: Class<*>): Method {
        return try {
            clazz.getDeclaredMethod(name, *paramCls)
        } catch (e: Exception) {
            try {
                clazz.getMethod(name, *paramCls)
            } catch (e: Exception) {
                getMethod(clazz.superclass, name, *paramCls)
            }
        }
    }

    companion object {
        private val dubboClassLoader =
            DubboClassLoader(DubboJars.jarUrls.toTypedArray(), DubboRequest::class.java.classLoader)
    }
}
