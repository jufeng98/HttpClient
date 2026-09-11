package org.javamaster.httpclient.ftp

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.util.lang.UrlClassLoader
import org.javamaster.httpclient.consts.HttpConsts.Companion.REPOSITORY_URL
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.utils.NotifyUtil
import org.javamaster.httpclient.utils.PluginUtils
import org.javamaster.httpclient.utils.RandomStringUtils
import org.javamaster.httpclient.utils.StreamUtils
import java.io.File
import java.io.InputStream
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.net.URL
import java.nio.file.Files
import java.util.concurrent.TimeUnit


/**
 * @author yudong
 */
@Suppress("DEPRECATION")
object FtpJars {
    @Volatile
    private var downloading = false

    private val jarUrls = mutableListOf<File>()
    private val jarMap = mutableMapOf<String, URL>()

    init {
        val ftpLibPath = getFtpLibPath()

        val listFiles = ftpLibPath.listFiles()

        listFiles?.forEach {
            jarUrls.add(it)
        }

        val classLoader = javaClass.getClassLoader()
        val addFiles = MethodHandles.lookup().findVirtual(
            classLoader.javaClass, "addFiles",
            MethodType.methodType(Void.TYPE, MutableList::class.java)
        )

        if (jarUrls.isNotEmpty()) {
            addFiles.invoke(classLoader, jarUrls.map { it.toPath() }.toList())
        }

        jarMap["ftpserver-core-1.2.1.jar"] =
            URL("$REPOSITORY_URL/org/apache/ftpserver/ftpserver-core/1.2.1/ftpserver-core-1.2.1.jar")
        jarMap["ftplet-api-4.0.1.jar"] =
            URL("$REPOSITORY_URL/org/apache/ftpserver/ftplet-api/1.2.1/ftplet-api-1.2.1.jar")
        jarMap["mina-core-2.2.4.jar"] =
            URL("$REPOSITORY_URL/org/apache/mina/mina-core/2.2.4/mina-core-2.2.4.jar")
    }

    fun jarsDownloaded(): Boolean {
        return jarUrls.size == jarMap.size
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

                                jarUrls.add(file)

                                indicator.fraction = (index + 1) * faction

                                if (index != jarMap.entries.size - 1) {
                                    TimeUnit.MILLISECONDS.sleep(500 + RandomStringUtils.RANDOM.nextLong(500))
                                }
                            }
                    }

                    val classLoader = javaClass.getClassLoader()
                    val addFiles = MethodHandles.lookup().findVirtual(
                        classLoader.javaClass, "addFiles",
                        MethodType.methodType(Void.TYPE, MutableList::class.java)
                    )

                    if (classLoader is UrlClassLoader) {
                        addFiles.invoke(classLoader, jarUrls.map { it.toPath() }.toList())
                    }

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

    private fun getFtpLibPath(): File {
        val pluginPath = PluginUtils.getPluginPath()
        return File(pluginPath, "lib/ftpLib")
    }
}
