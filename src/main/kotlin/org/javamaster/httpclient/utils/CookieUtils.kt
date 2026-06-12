package org.javamaster.httpclient.utils

import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiUtil
import org.apache.commons.lang3.time.DateUtils
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.factory.CookiePsiFactory
import org.javamaster.httpclient.js.support.jsObject.Cookie
import org.javamaster.httpclient.logger.HttpRequestLogger.logWarn
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.parser.CookieFile
import org.javamaster.httpclient.service.CookiesFileService
import org.javamaster.httpclient.utils.HttpUtils.computeReadAction
import java.net.HttpCookie
import java.net.URI
import java.net.http.HttpHeaders
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
        val cookiesFileService = project.getService(CookiesFileService::class.java)

        val cookiesFile = cookiesFileService.getCookiesFile() ?: return listOf()

        val cookiesPsiFile = computeReadAction { PsiUtil.getPsiFile(project, cookiesFile) as CookieFile }

        val currentTimeMillis = System.currentTimeMillis()
        return cookiesPsiFile.getRecords()
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

    fun createCookiesFileIfNotExists(project: Project) {
        val cookiesFileService = project.getService(CookiesFileService::class.java)

        val cookiesFile = cookiesFileService.getCookiesFile()
        if (cookiesFile == null) {
            cookiesFileService.createCookiesFile()
        }
    }

    fun saveCookiesToFile(cookies: List<Cookie>, project: Project): String {
        if (cookies.isEmpty()) {
            return ""
        }

        val cookiesFileService = project.getService(CookiesFileService::class.java)

        val cookiesFile = cookiesFileService.getCookiesFile() ?: return ""

        val cookiesPsiFile = computeReadAction { PsiUtil.getPsiFile(project, cookiesFile) as CookieFile }

        // 准备批次内去重的 key（在写操作外完成，减少写锁持有时间）
        val cookiesWithKey = cookies
            .map { it to "${it.domain}-${it.path}-${it.name}" }
            .distinctBy { it.second }

        var resultMessage = ""

        WriteCommandAction.runWriteCommandAction(project) {
            CommandProcessor.getInstance().runUndoTransparentAction {
                synchronized(cookiesPsiFile.virtualFile.path.intern()) {
                    try {
                        var cookieRecords = cookiesPsiFile.getRecords()

                        // 只保留 300 个 Cookie
                        val toRemoveCount = cookieRecords.size + cookies.size - 300
                        if (toRemoveCount > 0) {
                            cookieRecords.take(toRemoveCount).forEach {
                                it.prevSibling?.delete()
                                it.delete()
                            }

                            cookieRecords = cookiesPsiFile.getRecords()
                        }

                        val cookieMap = cookieRecords.associateBy {
                            "${it.domain.text}-${it.path.text}-${it.nameCk.text}"
                        }

                        for ((cookie, key) in cookiesWithKey) {
                            val record = CookiePsiFactory.createRecord(project, cookie)

                            val cookieRecord = cookieMap[key]
                            if (cookieRecord == null) {
                                cookiesPsiFile.add(record.prevSibling)
                                cookiesPsiFile.add(record)
                            } else {
                                cookieRecord.replace(record)
                            }
                        }

                        resultMessage = nls("cookie.saved", cookiesPsiFile.virtualFile.path)
                    } catch (e: Exception) {
                        logWarn("处理cookie出错", e)

                        resultMessage = "保存 cookie 到文件失败了: $e"
                    }
                }
            }
        }

        return resultMessage
    }

}
