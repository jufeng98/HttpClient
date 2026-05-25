package org.javamaster.httpclient.utils

import com.google.common.collect.Maps
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.util.PsiUtil
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.apache.commons.lang3.time.DateUtils
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.factory.CookiePsiFactory
import org.javamaster.httpclient.js.support.jsObject.Cookie
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.parser.CookieFile
import java.io.File
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
        reqHeaderMap: LinkedMultiValueMap<String, String>,
        domainCookieMap: Map<String, List<Cookie>>,
    ) {
        val uri = URI(url)
        val domain = uri.host
        val path = uri.path

        val cookies = domainCookieMap[domain]?.filter { path.startsWith(it.path) } ?: return

        val cookieValueStr = cookies.joinToString("; ") { "${it.name}=${it.value}" }

        val reqCookies = reqHeaderMap[com.google.common.net.HttpHeaders.COOKIE]
        if (reqCookies == null) {
            reqHeaderMap.add(com.google.common.net.HttpHeaders.COOKIE, cookieValueStr)
        } else {
            val reqCookieStr = reqCookies[0]
            reqHeaderMap.put(com.google.common.net.HttpHeaders.COOKIE, mutableListOf("$reqCookieStr; $cookieValueStr"))
        }
    }

    fun parseAll(url: String, headers: HttpHeaders): List<Cookie> {
        val setCookiesList = headers.allValues(com.google.common.net.HttpHeaders.SET_COOKIE)
        if (setCookiesList.isEmpty()) {
            return listOf()
        }

        val headersBuilder = Headers.Builder()
        setCookiesList.forEach {
            headersBuilder.add(com.google.common.net.HttpHeaders.SET_COOKIE, it)
        }
        val headerList = headersBuilder.build()

        val cookies = okhttp3.Cookie.parseAll(url.toHttpUrl(), headerList)
        return cookies.map {
            Cookie(it.domain, it.path, it.name, it.value, it.expiresAt, it.httpOnly, it.secure)
        }
    }

    fun getValidFileCookieMap(project: Project, refresh: Boolean): Map<String, List<Cookie>> {
        val cookieFile = createCookiesFileIfNotExists(project, refresh) ?: return mapOf()

        val currentTimeMillis = System.currentTimeMillis()
        return cookieFile.getRecords()
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
            .groupBy { it.domain }
    }

    fun createCookiesFileIfNotExists(project: Project, refresh: Boolean): CookieFile? {
        val historyFolder = InnerVariableEnum.HISTORY_FOLDER.exec("", project) ?: return null

        val historyDir = File(historyFolder)
        if (!historyDir.exists()) {
            Files.createDirectories(historyDir.toPath())
        }

        val cookiesFile = File(historyDir, HttpConsts.COOKIE_FILE_NAME)
        if (!cookiesFile.exists()) {
            val file = Files.createFile(cookiesFile.toPath())
            Files.writeString(file, "# domain\tpath\tname\tvalue\tdate")
        }

        val cookieVirtualFile = VfsUtil.findFileByIoFile(cookiesFile, refresh) ?: return null

        return PsiUtil.getPsiFile(project, cookieVirtualFile) as CookieFile
    }

    fun saveCookiesToFile(resCookies: List<Cookie>, project: Project): String {
        val cookiePsiFile = createCookiesFileIfNotExists(project, true) ?: return ""

        val cookies = resCookies.filter { it.expiresAt > 0 }

        val cookieRecords = cookiePsiFile.getRecords()

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

            val cookieMap = Maps.uniqueIndex(cookieRecords) { "${it.domain.text}-${it.path.text}-${it.nameCk.text}" }

            cookies.forEach {
                val record = CookiePsiFactory.createRecord(project, it)

                val key = "${it.domain}-${it.path}-${it.name}"
                val cookieRecord = cookieMap[key]
                if (cookieRecord == null) {
                    cookiePsiFile.add(record.prevSibling)
                    cookiePsiFile.add(record)
                } else {
                    cookieRecord.replace(record)
                }
            }
        }

        return if (cookies.isNotEmpty()) nls("cookie.saved", cookiePsiFile.virtualFile.path) else ""
    }

}
