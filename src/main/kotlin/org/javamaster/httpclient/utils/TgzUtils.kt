package org.javamaster.httpclient.utils

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * @author yudong
 */
object TgzUtils {

    fun extract(tgzFilePath: String, outputDir: String) {
        FileInputStream(tgzFilePath).use { fis ->
            GzipCompressorInputStream(fis).use { gzip ->
                TarArchiveInputStream(gzip).use { tarIn ->
                    @Suppress("DEPRECATION", "DEPRECATION")
                    var entry = tarIn.nextTarEntry
                    while (entry != null) {
                        val outputFile = File(outputDir, entry.name)

                        if (entry.isDirectory) {
                            outputFile.mkdirs()
                        } else {
                            outputFile.parentFile.mkdirs()
                            FileOutputStream(outputFile).use { fos ->
                                IOUtils.copy(tarIn, fos)
                            }
                        }

                        @Suppress("DEPRECATION", "DEPRECATION")
                        entry = tarIn.nextTarEntry
                    }
                }
            }
        }
    }

}