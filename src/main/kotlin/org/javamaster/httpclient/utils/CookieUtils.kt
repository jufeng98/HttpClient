package org.javamaster.httpclient.utils

import com.google.common.collect.Maps
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.util.PsiUtil
import org.apache.commons.lang3.time.DateUtils
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.factory.CookiePsiFactory
import org.javamaster.httpclient.js.support.jsObject.Cookie
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.parser.CookieFile
import java.io.File
import java.net.HttpCookie
import java.net.URI
import java.net.http.HttpHeaders
import java.nio.file.Files
import java.util.*


/**
 * @author yudong
 */
object CookieUtils {

    fun addFileCookieToReqHeader(
        url: String,
        reqHeaderMap: LinkedMultiValueMap<String, String?>,
        fileCookies: List<Cookie>,
    ) {
        val uri = URI(url)
        val domain = uri.host
        val path = uri.path

        val cookies = fileCookies
            .filter { domain.endsWith(it.domain) }
            .filter { path.startsWith(it.path) }
        if (cookies.isEmpty()) {
            return
        }

        val cookieValueStr = cookies.joinToString("; ") { "${it.name}=${it.value}" }

        val reqCookies = reqHeaderMap[com.google.common.net.HttpHeaders.COOKIE]
        if (reqCookies == null) {
            reqHeaderMap.add(com.google.common.net.HttpHeaders.COOKIE, cookieValueStr)
        } else {
            val reqCookieStr = reqCookies[0]
            reqHeaderMap.put(
                com.google.common.net.HttpHeaders.COOKIE,
                mutableListOf<String?>("$reqCookieStr; $cookieValueStr")
            )
        }
    }

    fun parseAll(url: String, headers: HttpHeaders): List<Cookie> {
        val setCookiesList = headers.allValues(com.google.common.net.HttpHeaders.SET_COOKIE)
        if (setCookiesList.isEmpty()) {
            return listOf()
        }

        return setCookiesList
            .map {
                val cookies = HttpCookie.parse(com.google.common.net.HttpHeaders.SET_COOKIE + ": " + it)

                cookies
                    .map { innerIt ->
                        val domain = innerIt.domain ?: URI(url).host

                        val millis = System.currentTimeMillis()
                        val maxAge = innerIt.maxAge
                        val expiresAt = if (maxAge == -1L) Long.MAX_VALUE else millis + maxAge * 1000

                        Cookie(
                            domain, innerIt.path, innerIt.name, innerIt.value,
                            expiresAt, innerIt.isHttpOnly, innerIt.secure
                        )
                    }
            }
            .flatten()
    }

    fun getValidFileCookieMap(project: Project): List<Cookie> {
        val cookieFile = getCookiesFilePath(project) ?: return emptyList()

        val cookieVirtualFile = VfsUtil.findFileByIoFile(cookieFile, false) ?: return emptyList()

        val cookiePsiFile = PsiUtil.getPsiFile(project, cookieVirtualFile) as CookieFile

        val currentTimeMillis = System.currentTimeMillis()
        return cookiePsiFile.getRecords()
            .mapNotNull {
                val dateTxt = it.date.text
                val expiresAt = if (dateTxt == "-1") {
                    Long.MAX_VALUE
                } else {
                    val date = DateUtils.parseDate(dateTxt, Locale.ENGLISH, HttpConsts.JS_DATE_PATTERN)
                    date.time
                }

                if (currentTimeMillis > expiresAt) {
                    return@mapNotNull null
                }

                Cookie(it.domain.text, it.path.text, it.nameCk.text, it.value.text, expiresAt, false, false)
            }
    }

    fun getCookiesFilePath(project: Project): File? {
        val historyFolder = InnerVariableEnum.HISTORY_FOLDER.exec("", project) ?: return null

        return File(historyFolder, HttpConsts.COOKIE_FILE_NAME)
    }

    fun createCookiesFileIfNotExists(project: Project) {
        val historyFolder = InnerVariableEnum.HISTORY_FOLDER.exec("", project) ?: return

        val historyDir = File(historyFolder)
        if (!historyDir.exists()) {
            Files.createDirectories(historyDir.toPath())
        }

        val cookiesFile = File(historyDir, HttpConsts.COOKIE_FILE_NAME)
        if (!cookiesFile.exists()) {
            val file = Files.createFile(cookiesFile.toPath())
            Files.writeString(file, "# domain\tpath\tname\tvalue\tdate")
        }
    }

    fun saveCookiesToFile(cookies: List<Cookie>, project: Project, cookiesPsiFile: CookieFile?): String {
        cookiesPsiFile ?: return ""

        val cookieRecords = runReadAction { cookiesPsiFile.getRecords() }

        runInEdt {
            runWriteAction {
                CommandProcessor.getInstance().runUndoTransparentAction {
                    // 只保留 300 个 Cookie
                    val size = cookieRecords.size + cookies.size - 300
                    if (size > 0) {
                        cookieRecords.subList(0, Integer.min(size, cookieRecords.size))
                            .forEach {
                                it.prevSibling.delete()
                                it.delete()
                            }
                    }

                    val cookieMap =
                        Maps.uniqueIndex(cookieRecords) { "${it.domain.text}-${it.path.text}-${it.nameCk.text}" }

                    cookies.forEach {
                        val record = CookiePsiFactory.createRecord(project, it)

                        val key = "${it.domain}-${it.path}-${it.name}"
                        val cookieRecord = cookieMap[key]
                        if (cookieRecord == null) {
                            cookiesPsiFile.add(record.prevSibling)
                            cookiesPsiFile.add(record)
                        } else {
                            cookieRecord.replace(record)
                        }
                    }
                }
            }
        }

        return if (cookies.isNotEmpty()) nls("cookie.saved", cookiesPsiFile.virtualFile.path) else ""
    }

}
