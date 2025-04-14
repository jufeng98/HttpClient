package org.javamaster.httpclient.dashboard.support

import com.intellij.ide.plugins.PluginManagerCore.getPlugin
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.components.Service
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.util.application
import com.intellij.util.io.DigestUtil.random
import org.javamaster.httpclient.model.PreJsFile
import org.javamaster.httpclient.utils.NotifyUtil
import org.javamaster.httpclient.utils.StreamUtils
import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit

/**
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class JsTgz(private val project: Project) {
    private val jsTgzFileMap = mutableMapOf<String, File>()

    init {
        val jsLibPath = getJsLibPath()

        val listFiles = jsLibPath.listFiles()

        listFiles?.forEach {
            if (it.isDirectory) {
                return@forEach
            }

            jsTgzFileMap[it.name] = it
        }
    }


    fun tryInitPreJsFilesFromLocal(preJsFiles: List<PreJsFile>): Boolean {
        preJsFiles.forEach {
            val name = File(it.url!!.toString()).name
            val tgzJsFile = jsTgzFileMap[name] ?: return false

            val nameWithoutExtension = File(it.url.toString()).nameWithoutExtension

            val libDir = File(tgzJsFile.parentFile, nameWithoutExtension)
            if (!libDir.exists() || !libDir.isDirectory) {
                return false
            }

            it.file = findTargetJsFile(libDir) ?: return false
        }

        return true
    }

    private fun findTargetJsFile(libDir: File): File? {
        val packJson = File(libDir, "package" + File.separator + "package.json")
        val virtualFile = VfsUtil.findFileByIoFile(packJson, false) ?: return null
        val jsonFile = PsiUtil.getPsiFile(project, virtualFile) as JsonFile

        val jsonObject = jsonFile.topLevelValue
        if (jsonObject !is JsonObject) {
            return null
        }

        val jsonStringLiteral = jsonObject.findProperty("main")?.value as JsonStringLiteral? ?: return null

        val entryJs = jsonStringLiteral.value

        val file = File(packJson.parentFile, entryJs)
        if (!file.exists() || file.isDirectory) {
            return null
        }

        return file
    }

    fun downloadAsync(project: Project, npmFiles: List<PreJsFile>) {
        object : Task.Backgroundable(project, "Downloading js libraries...", true) {
            override fun run(indicator: ProgressIndicator) {
                val jsLibPath = getJsLibPath()
                if (jsLibPath.exists()) {
                    jsLibPath.deleteRecursively()
                }

                jsLibPath.mkdirs()

                jsTgzFileMap.clear()

                var errorMsg: String? = null
                val faction = 1.0 / npmFiles.size

                for ((index, entry) in npmFiles.withIndex()) {
                    val npmFile = File(entry.url!!.toString())

                    val name = npmFile.name
                    val nameWithoutExtension = npmFile.nameWithoutExtension
                    val url = entry.url

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

                                val file = File(jsLibPath, name)

                                Files.write(file.toPath(), byteArray)

                                jsTgzFileMap[name] = file

                                println("Downloaded js library $name : $file")

                                val outputDir = File(jsLibPath.absolutePath, nameWithoutExtension)
                                outputDir.mkdirs()

                                TgzExtractor.extract(file.absolutePath, outputDir.absolutePath)

                                println("Extracted js library $file to $outputDir")

                                indicator.fraction = (index + 1) * faction
                            } catch (e: Exception) {
                                errorMsg =
                                    "Downloaded js libraries error, please try again. url: $url, error msg: ${e.message}"
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
                    application.invokeLater {
                        NotifyUtil.notifyCornerSuccess(project, "Js libraries have been successfully downloaded!")
                    }
                }
            }
        }.queue()
    }

    private fun getJsLibPath(): File {
        val pluginDescriptor = getPlugin(PluginId.findId("org.javamaster.HttpRequest"))
        val pluginPath = pluginDescriptor!!.pluginPath.toFile()
        return File(pluginPath, "lib/jsLib")
    }
}
