package org.javamaster.httpclient.dubbo.support

import com.intellij.ide.plugins.PluginManagerCore.getPlugin
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.util.application
import org.javamaster.httpclient.dubbo.DubboRequest
import org.javamaster.httpclient.dubbo.loader.DubboClassLoader
import org.javamaster.httpclient.utils.NotifyUtil
import org.javamaster.httpclient.utils.StreamUtils
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author yudong
 */
object DubboJars {
    var dubboClassLoader: DubboClassLoader

    private const val REPOSITORY_URL = "https://maven.aliyun.com/nexus/content/groups/public"

    private val jarUrls = mutableListOf<URL>()
    private val jarMap = mutableMapOf<String, URL>()
    private val random = Random()

    init {
        jarUrls.addAll(findPluginJarUrls())

        val dubboLibPath = getDubboLibPath()

        val listFiles = dubboLibPath.listFiles()

        listFiles?.forEach {
            jarUrls.add(it.toURI().toURL())
        }

        dubboClassLoader = DubboClassLoader(jarUrls.toTypedArray(), DubboJars::class.java.classLoader)

        jarMap["javassist-3.30.2-GA.jar"] =
            URL("$REPOSITORY_URL/org/javassist/javassist/3.30.2-GA/javassist-3.30.2-GA.jar")
        jarMap["curator-client-4.0.1.jar"] =
            URL("$REPOSITORY_URL/org/apache/curator/curator-client/4.0.1/curator-client-4.0.1.jar")
        jarMap["curator-framework-4.0.1.jar"] =
            URL("$REPOSITORY_URL/org/apache/curator/curator-framework/4.0.1/curator-framework-4.0.1.jar")
        jarMap["netty-3.10.5.Final.jar"] =
            URL("$REPOSITORY_URL/io/netty/netty/3.10.5.Final/netty-3.10.5.Final.jar")
        jarMap["zookeeper-3.5.3-beta.jar"] =
            URL("$REPOSITORY_URL/org/apache/zookeeper/zookeeper/3.5.3-beta/zookeeper-3.5.3-beta.jar")
    }

    fun jarsDownloaded(): Boolean {
        return jarUrls.size == jarMap.size + 2
    }

    fun downloadAsync(project: Project) {
        object : Task.Backgroundable(project, "Downloading dubbo dependencies...", true) {
            override fun run(indicator: ProgressIndicator) {
                val dubboLibPath = getDubboLibPath()
                if (!dubboLibPath.exists()) {
                    dubboLibPath.mkdirs()
                }

                jarUrls.clear()

                var errorMsg: String? = null
                val faction = 1.0 / jarMap.size

                for ((index, entry) in jarMap.entries.withIndex()) {
                    val name = entry.key
                    val url = entry.value

                    if (errorMsg != null) {
                        break
                    }

                    url.openStream()
                        .use {
                            try {
                                if (indicator.isCanceled) {
                                    errorMsg = "Download aborted!"
                                    return@use
                                }

                                val byteArray = StreamUtils.copyToByteArray(it)

                                val file = File(dubboLibPath, name)

                                if (file.exists()) {
                                    file.delete()
                                    println("deleted exists jar file: $file")
                                }

                                Files.write(file.toPath(), byteArray)

                                jarUrls.add(file.toURI().toURL())

                                indicator.fraction = (index + 1) * faction

                                println("Downloaded dubbo jar $name : $file")
                            } catch (e: Exception) {
                                errorMsg =
                                    "Downloaded dubbo dependencies error, please try again. url: $url, error msg: ${e.message}"
                                e.printStackTrace()
                            }

                            TimeUnit.MILLISECONDS.sleep(1000 + random.nextLong(2000))
                        }
                }

                indicator.fraction = 1.0

                if (errorMsg != null) {
                    application.invokeLater {
                        NotifyUtil.notifyCornerWarn(project, errorMsg!!)
                    }
                } else {
                    dubboClassLoader.close()

                    jarUrls.addAll(findPluginJarUrls())

                    dubboClassLoader = DubboClassLoader(jarUrls.toTypedArray(), DubboRequest::class.java.classLoader)

                    application.invokeLater {
                        NotifyUtil.notifyCornerSuccess(project, "Dubbo dependencies have been successfully downloaded!")
                    }
                }
            }
        }.queue()
    }

    private fun findPluginJarUrls(): List<URL> {
        val dubboLibPath = getDubboLibPath()
        val libPath = dubboLibPath.parentFile
        return libPath.listFiles()!!
            .filter { it.name.startsWith("instrumented-HttpRequest") || it.name == "dubbo-2.6.12.jar" }
            .map { it.toURI().toURL() }
    }

    private fun getDubboLibPath(): File {
        val pluginDescriptor = getPlugin(PluginId.findId("org.javamaster.HttpRequest"))
        val pluginPath = pluginDescriptor!!.pluginPath.toFile()
        return File(pluginPath, "lib/dubboLib")
    }
}
