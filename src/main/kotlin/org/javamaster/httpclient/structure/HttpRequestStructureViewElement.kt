package org.javamaster.httpclient.structure

import com.intellij.icons.AllIcons
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.navigation.ColoredItemPresentation
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.HttpRequestBlock
import org.javamaster.httpclient.psi.impl.HttpPsiImplUtil
import org.javamaster.httpclient.psi.impl.HttpPsiImplUtil.getHeaderFieldOption
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.getTabName
import javax.swing.Icon

class HttpRequestStructureViewElement private constructor(
    element: PsiElement, private val myPresentationText: String, private val myLocation: String?,
    private val myIcon: Icon?, private val myIsValid: Boolean,
) : PsiTreeElementBase<PsiElement?>(element), ColoredItemPresentation {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element
        if (element is HttpFile) {
            val blocks = element.getRequestBlocks()
            if (blocks.isEmpty()) {
                return emptyList()
            }

            val children: MutableList<StructureViewTreeElement> = mutableListOf()
            val globalHandler = element.getGlobalHandler()
            if (globalHandler != null) {
                children.add(create(globalHandler, NlsBundle.nls("global.handler"), AllIcons.Actions.Play_first))
            }

            val globalVariables = element.getGlobalVariables()
            globalVariables.forEach {
                children.add(
                    create(
                        it,
                        NlsBundle.nls("global.variable"),
                        it.text,
                        AllIcons.General.InlineVariables,
                        true
                    )
                )
            }

            for (block in blocks) {
                val list = Companion.getChildren(block)
                children.addAll(list)
            }
            return children
        } else if (element is HttpRequestBlock) {
            return getChildren(element)
        }

        return emptyList()
    }

    override fun getLocationString(): String? {
        return myLocation
    }

    override fun isSearchInLocationString(): Boolean {
        return true
    }

    override fun getTextAttributesKey(): TextAttributesKey? {
        return if (myIsValid) null else CodeInsightColors.ERRORS_ATTRIBUTES
    }

    override fun getPresentableText(): String {
        return myPresentationText
    }

    override fun getIcon(open: Boolean): Icon? {
        return myIcon ?: super.getIcon(open)
    }

    companion object {
        private val imageContentSet = setOf(
            ContentType.IMAGE_PNG,
            ContentType.IMAGE_JPEG,
            ContentType.IMAGE_BMP,
            ContentType.IMAGE_WEBP,
            ContentType.IMAGE_SVG,
            ContentType.IMAGE_GIF,
            ContentType.IMAGE_TIFF,
        )

        fun create(element: PsiElement, text: String?, icon: Icon?): StructureViewTreeElement {
            return create(element, text, null, icon)
        }

        fun create(
            element: PsiElement, text: String?,
            location: String?, icon: Icon?,
        ): StructureViewTreeElement {
            return HttpRequestStructureViewElement(
                element, StringUtil.notNullize(text, NlsBundle.nls("not.defined")),
                location, icon, StringUtil.isNotEmpty(text)
            )
        }

        fun create(
            element: PsiElement, text: String,
            location: String?, icon: Icon, isValid: Boolean,
        ): StructureViewTreeElement {
            return HttpRequestStructureViewElement(element, text, location, icon, isValid)
        }

        private fun getChildren(block: HttpRequestBlock): List<StructureViewTreeElement> {
            val children = mutableListOf<StructureViewTreeElement>()
            val request = block.request
            val originalHost = request.httpHost
            val target = request.requestTarget
            val path = target?.url
            val project = block.project

            val preRequestHandler = block.preRequestHandler
            if (preRequestHandler != null) {
                children.add(create(preRequestHandler, NlsBundle.nls("pre.handler"), AllIcons.Actions.Play_first))
            }

            val location = StringBuilder()
            location.append(StringUtil.notNullize(path, NlsBundle.nls("not.defined")))

            val tabName: String
            val method = request.method
            tabName = getTabName(method)

            val icon = HttpUtils.pickMethodIcon(method.text)

            children.add(
                create(request, tabName, location.toString(), icon, StringUtil.isNotEmpty(originalHost))
            )

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
                    name = getHeaderFieldOption(headerFieldValue, "name") ?: ""
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

        private fun isImageType(contentType: ContentType?): Boolean {
            return imageContentSet.contains(contentType ?: return false)
        }

        private fun getInjectedLanguageIcon(project: Project, messageBody: HttpMessageBody?): Icon? {
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
}
