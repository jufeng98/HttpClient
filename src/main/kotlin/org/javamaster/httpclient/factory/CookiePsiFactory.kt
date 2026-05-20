package org.javamaster.httpclient.factory

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import org.apache.commons.lang3.time.DateFormatUtils
import org.javamaster.httpclient.CookieFileType
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.js.support.jsObject.Cookie
import org.javamaster.httpclient.parser.CookieFile
import org.javamaster.httpclient.psi.CookieRecord
import java.util.*


/**
 * @author yudong
 */
object CookiePsiFactory {

    fun createRecord(project: Project, cookie: Cookie): CookieRecord {
        val expiresAt = cookie.expiresAt
        val limit = System.currentTimeMillis() + 3L * 365 * 86400 * 1000
        val format = if (expiresAt > limit) {
            "-1"
        } else {
            val date = Date(expiresAt)
            DateFormatUtils.format(date, HttpConsts.JS_DATE_PATTERN, Locale.ENGLISH)
        }

        val recordStr = "\n${cookie.domain}\t${cookie.path}\t${cookie.name}\t${cookie.value}\t$format"
        val psiFile = createDummyFile(project, recordStr)

        return PsiTreeUtil.getChildOfType(psiFile, CookieRecord::class.java)!!
    }

    private fun createDummyFile(project: Project, content: String): CookieFile {
        val fileType = CookieFileType.INSTANCE
        val fileName = "dummy." + fileType.defaultExtension
        return PsiFileFactory.getInstance(project)
            .createFileFromText(fileName, fileType, content, System.currentTimeMillis(), false) as CookieFile
    }

}
