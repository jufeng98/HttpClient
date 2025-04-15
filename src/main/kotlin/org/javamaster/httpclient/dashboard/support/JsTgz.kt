package org.javamaster.httpclient.dashboard.support

import com.intellij.ide.plugins.PluginManagerCore.getPlugin
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.testFramework.LightVirtualFile
import com.intellij.util.application
import com.intellij.util.io.DigestUtil.random
import org.javamaster.httpclient.model.PreJsFile
import org.javamaster.httpclient.utils.NotifyUtil
import org.javamaster.httpclient.utils.StreamUtils
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.util.concurrent.TimeUnit

/**
 * @author yudong
 */
object JsTgz {
    private var downloading = false
    private val jsTgzFileMap = mutableMapOf<String, File>()
    private val packageJsonMainJsFileMap = mutableMapOf<String, File>()

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


    fun jsLibrariesNotDownloaded(npmFiles: List<PreJsFile>): List<PreJsFile> {
        return npmFiles.filter {
            val name = it.urlFile!!.name
            jsTgzFileMap[name] == null
        }
    }

    fun initJsLibrariesFile(npmFiles: List<PreJsFile>, project: Project) {
        npmFiles.forEach {
            val urlFile = it.urlFile!!

            val name = urlFile.name
            val nameWithoutExtension = urlFile.nameWithoutExtension

            var file = packageJsonMainJsFileMap[name]
            if (file == null) {
                val tgzJsFile = jsTgzFileMap[name]!!

                val libDir = File(tgzJsFile.parentFile, nameWithoutExtension)

                file = findPackageJsonMainJsFile(libDir, project)

                packageJsonMainJsFileMap[name] = file

                println("Cache the main js entry $file of the $name")
            }

            it.file = file
        }
    }

    fun initJsLibrariesVirtualFile(preJsFiles: List<PreJsFile>) {
        preJsFiles.forEach {
            val virtualFile = VfsUtil.findFileByIoFile(it.file, true)
            it.virtualFile = virtualFile!!
        }
    }

    fun downloadAsync(project: Project, npmFiles: List<PreJsFile>) {
        if (downloading) {
            NotifyUtil.notifyCornerWarn(project, "Download not finished yet!")
            return
        }

        downloading = true

        NotifyUtil.notifyCornerSuccess(
            project,
            "Js libraries not loaded yet, Start downloading libraries. When finished, please try again."
        )

        object : Task.Backgroundable(project, "Downloading js libraries...", true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val jsLibPath = getJsLibPath()

                    if (!jsLibPath.exists()) {
                        jsLibPath.mkdirs()
                    }

                    val faction = 1.0 / npmFiles.size

                    for ((index, entry) in npmFiles.withIndex()) {
                        val npmFile = File(entry.url!!.toString())

                        val url = entry.url

                        url.openStream()
                            .use {
                                if (indicator.isCanceled) {
                                    throw RuntimeException("Download aborted!")
                                }

                                saveAndExtract(it, npmFile, jsLibPath)

                                indicator.fraction = (index + 1) * faction

                                TimeUnit.MILLISECONDS.sleep(1000 + random.nextLong(2000))
                            }
                    }

                    application.invokeLater {
                        NotifyUtil.notifyCornerSuccess(
                            project,
                            "Js libraries have been successfully downloaded!"
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()

                    application.invokeLater {
                        NotifyUtil.notifyCornerError(
                            project,
                            "Downloaded js libraries error, please try again, error msg: $e"
                        )
                    }
                } finally {
                    downloading = false
                }
            }
        }.queue()
    }

    private fun saveAndExtract(inputStream: InputStream, npmFile: File, jsLibPath: File) {
        val name = npmFile.name
        val nameWithoutExtension = npmFile.nameWithoutExtension

        val byteArray = StreamUtils.copyToByteArray(inputStream)

        val file = File(jsLibPath, name)

        if (file.exists()) {
            file.delete()
            println("Deleted exists file: $file")
        }

        Files.write(file.toPath(), byteArray)
        println("Downloaded js library ${file.name} : $file")

        val outputDir = File(jsLibPath.absolutePath, nameWithoutExtension)

        if (outputDir.exists()) {
            outputDir.deleteRecursively()
            println("Deleted exists dir ${outputDir.name}: $outputDir")
        }

        outputDir.mkdirs()

        TgzExtractor.extract(file.absolutePath, outputDir.absolutePath)

        println("Extracted js library $file to $outputDir")

        jsTgzFileMap[name] = file
    }

    private fun findPackageJsonMainJsFile(libDir: File, project: Project): File {
        val packJson = File(libDir, "package" + File.separator + "package.json")

        if (!packJson.exists()) {
            throw IllegalArgumentException("Invalid library: ${libDir.name}")
        }

        val packageJsonStr = Files.readString(packJson.toPath())

        val virtualFile = LightVirtualFile("dummy.json", packageJsonStr)

        val jsonFile = PsiUtil.getPsiFile(project, virtualFile) as JsonFile

        val jsonObject = jsonFile.topLevelValue
        if (jsonObject !is JsonObject) {
            throw IllegalArgumentException("Invalid library: ${libDir.name}")
        }

        val jsonStringLiteral = jsonObject.findProperty("main")?.value as JsonStringLiteral?
            ?: throw IllegalArgumentException("Invalid library: ${libDir.name}")

        val entryJs = jsonStringLiteral.value

        val file = File(packJson.parentFile, entryJs)
        if (!file.exists() || file.isDirectory) {
            throw IllegalArgumentException("Invalid library: ${libDir.name}")
        }

        return file
    }

    private fun getJsLibPath(): File {
        val pluginDescriptor = getPlugin(PluginId.findId("org.javamaster.HttpRequest"))
        val pluginPath = pluginDescriptor!!.pluginPath.toFile()
        return File(pluginPath, "lib/jsLib")
    }
}
