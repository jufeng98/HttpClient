package org.javamaster.httpclient.ftp

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.utils.NotifyUtil
import org.javamaster.httpclient.utils.PluginUtils
import org.javamaster.httpclient.utils.RandomStringUtils
import org.javamaster.httpclient.utils.StreamUtils
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.util.concurrent.TimeUnit

/**
 * @author yudong
 */
@Suppress("DEPRECATION")
object FtpJars {
    var ftpClassLoader: FtpClassLoader

    @Volatile
    private var downloading = false

    private const val REPOSITORY_URL = "https://maven.aliyun.com/nexus/content/groups/public"

    private val jarUrls = mutableListOf<URL>()
    private val jarMap = mutableMapOf<String, URL>()

    init {
        jarUrls.addAll(findPluginJarUrls())

        val ftpLibPath = getFtpLibPath()

        val listFiles = ftpLibPath.listFiles()

        listFiles?.forEach {
            jarUrls.add(it.toURI().toURL())
        }

        ftpClassLoader = FtpClassLoader(jarUrls.toTypedArray(), FtpJars::class.java.classLoader)

        jarMap["ftpserver-core-1.2.1.jar"] =
            URL("$REPOSITORY_URL/org/apache/ftpserver/ftpserver-core/1.2.1/ftpserver-core-1.2.1.jar")
        jarMap["ftplet-api-4.0.1.jar"] =
            URL("$REPOSITORY_URL/org/apache/ftpserver/ftplet-api/1.2.1/ftplet-api-1.2.1.jar")
        jarMap["mina-core-2.2.4.jar"] =
            URL("$REPOSITORY_URL/org/apache/mina/mina-core/2.2.4/mina-core-2.2.4.jar")
    }

    fun jarsDownloaded(): Boolean {
        return jarUrls.size == jarMap.size + 1
    }

    fun downloadAsync(project: Project) {
        if (downloading) {
            NotifyUtil.notifyCornerWarn(project, NlsBundle.nls("download.not.finish"))
            return
        }

        downloading = true

        NotifyUtil.notifyCornerSuccess(project, NlsBundle.nls("start.download"))

        object : Task.Backgroundable(project, NlsBundle.nls("ftp.downloading"), true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val ftpLibPath = getFtpLibPath()

                    if (!ftpLibPath.exists()) {
                        ftpLibPath.mkdirs()
                    }

                    jarUrls.clear()

                    val faction = 1.0 / jarMap.size

                    for ((index, entry) in jarMap.entries.withIndex()) {
                        val name = entry.key
                        val url = entry.value

                        url.openStream()
                            .use {
                                if (indicator.isCanceled) {
                                    throw RuntimeException(NlsBundle.nls("download.abort"))
                                }

                                val file = saveToFile(it, name, ftpLibPath)

                                jarUrls.add(file.toURI().toURL())

                                indicator.fraction = (index + 1) * faction

                                if (index != jarMap.entries.size - 1) {
                                    TimeUnit.MILLISECONDS.sleep(500 + RandomStringUtils.RANDOM.nextLong(500))
                                }
                            }
                    }

                    ftpClassLoader.close()

                    jarUrls.addAll(findPluginJarUrls())

                    ftpClassLoader = FtpClassLoader(jarUrls.toTypedArray(), FtpJars::class.java.classLoader)

                    NotifyUtil.notifyCornerSuccess(project, NlsBundle.nls("ftp.downloaded"))
                } catch (e: Exception) {
                    e.printStackTrace()

                    NotifyUtil.notifyCornerWarn(project, NlsBundle.nls("ftp.downloaded.error") + " ${e.message}")
                } finally {
                    downloading = false
                }

            }
        }.queue()
    }

    private fun saveToFile(inputStream: InputStream, name: String, ftpLibPath: File): File {
        val byteArray = StreamUtils.copyToByteArray(inputStream)

        val file = File(ftpLibPath, name)

        if (file.exists()) {
            file.delete()
            println("deleted exists jar file: $file")
        }

        Files.write(file.toPath(), byteArray)

        println("Downloaded ftp jar $name : $file")

        return file
    }

    private fun findPluginJarUrls(): List<URL> {
        val ftpLibPath = getFtpLibPath()
        val libPath = ftpLibPath.parentFile
        return libPath.listFiles()!!
            .filter { it.name.contains("HttpRequest") && Files.size(it.toPath()) > 800000 }
            .map { it.toURI().toURL() }
    }

    private fun getFtpLibPath(): File {
        val pluginPath = PluginUtils.getPluginPath()
        return File(pluginPath, "lib/ftpLib")
    }
}
