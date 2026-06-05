package org.javamaster.httpclient.utils

import com.intellij.icons.AllIcons
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.HttpRequestBlock
import org.javamaster.httpclient.psi.impl.HttpPsiImplUtil
import org.javamaster.httpclient.structure.support.HttpRequestStructureViewElement
import javax.swing.Icon

/**
 * @author yudong
 */
object StructureUtils {

    fun create(element: HttpRequestBlock): HttpRequestStructureViewElement {
        val request = element.request
        if (request == null) {
            return HttpRequestStructureViewElement(element, NlsBundle.nls("not.defined"), null, null, false)
        }

        val method = request.method
        val tabName = HttpUtils.getTabName(method)
        val icon = MyPsiUtils.pickMethodIcon(method.text)

        return HttpRequestStructureViewElement(element, tabName, null, icon, true)
    }

    fun create(element: PsiElement, text: String?, icon: Icon?): StructureViewTreeElement {
        return HttpRequestStructureViewElement(
            element, StringUtil.notNullize(text, NlsBundle.nls("not.defined")),
            null, icon, StringUtil.isNotEmpty(text)
        )
    }

    fun create(
        element: PsiElement, text: String,
        location: String?, icon: Icon?, isValid: Boolean,
    ): StructureViewTreeElement {
        return HttpRequestStructureViewElement(element, text, location, icon, isValid)
    }

    fun getRequestBlockChildren(block: HttpRequestBlock): List<StructureViewTreeElement> {
        val children = mutableListOf<StructureViewTreeElement>()
        val request = block.request ?: return children

        val originalHost = request.httpHost
        val requestTarget = request.requestTarget
        val path = requestTarget?.url
        val project = block.project

        val preRequestHandler = block.preRequestHandler
        if (preRequestHandler != null) {
            children.add(create(preRequestHandler, NlsBundle.nls("pre.handler"), AllIcons.Actions.Play_first))
        }

        val location = StringUtil.notNullize(path, NlsBundle.nls("not.defined"))

        children.add(create(request, "", location, null, StringUtil.isNotEmpty(originalHost)))

        val body = request.body
        val messagesGroup = body?.requestMessagesGroup
        if (messagesGroup != null) {
            var mimeType = NlsBundle.nls("not.defined")
            val contentType = request.contentType
            if (contentType != null) {
                mimeType = contentType.mimeType
            }

            val bodyIcon = if (isImageType(contentType)) {
                HttpIcons.IMAGE
            } else {
                getInjectedLanguageIcon(project, messagesGroup.messageBody)
            }

            children.add(create(messagesGroup, NlsBundle.nls("request.body") + " $mimeType", bodyIcon))
        }

        val multipartMessage = body?.multipartMessage
        multipartMessage?.multipartFieldList?.forEach {
            var name = ""
            val headerFieldValue = HttpPsiImplUtil.getMultipartFieldDescription(it)
            if (headerFieldValue != null) {
                name = HttpPsiImplUtil.getHeaderFieldOption(headerFieldValue, "name") ?: ""
            }

            var mimeType = NlsBundle.nls("not.defined")
            val contentType = it.contentType
            if (contentType != null) {
                mimeType = contentType.mimeType
            }

            val multiIcon = if (isImageType(contentType)) {
                HttpIcons.IMAGE
            } else {
                getInjectedLanguageIcon(project, it.requestMessagesGroup.messageBody)
            }

            children.add(create(it, NlsBundle.nls("multipart.field") + ": $name $mimeType", multiIcon))
        }

        val responseHandler = request.responseHandler
        if (responseHandler != null) {
            children.add(create(responseHandler, NlsBundle.nls("response.handler"), AllIcons.Actions.Play_last))
        }

        return children
    }

    fun isImageType(contentType: ContentType?): Boolean {
        return HttpConsts.imageContentSet.contains(contentType ?: return false)
    }

    fun getInjectedLanguageIcon(project: Project, messageBody: HttpMessageBody?): Icon? {
        if (messageBody == null) {
            return null
        }

        val injectedLanguageManager = InjectedLanguageManager.getInstance(project)
        val files = injectedLanguageManager.getInjectedPsiFiles(messageBody)
        val psiElement = files?.get(0)?.first
        val psiFile = psiElement as PsiFile?
        return psiFile?.getIcon(Iconable.ICON_FLAG_VISIBILITY)
    }

}
