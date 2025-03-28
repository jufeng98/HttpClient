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
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.HttpRequestBlock
import org.javamaster.httpclient.psi.impl.HttpPsiImplUtil
import org.javamaster.httpclient.psi.impl.HttpPsiImplUtil.getHeaderFieldOption
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
                children.add(create(globalHandler, "Global handler", AllIcons.Actions.Play_first))
            }

            val globalVariables = element.getGlobalVariables()
            globalVariables.forEach {
                children.add(
                    create(
                        it,
                        "Global Variable",
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

        fun create(element: PsiElement, text: String?, icon: Icon?): StructureViewTreeElement {
            return create(element, text, null, icon)
        }

        fun create(
            element: PsiElement, text: String?,
            location: String?, icon: Icon?,
        ): StructureViewTreeElement {
            return HttpRequestStructureViewElement(
                element, StringUtil.notNullize(text, "<not defined>"),
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
            val originalHost = request?.httpHost ?: return children
            val target = request.requestTarget
            val path = target?.url
            val project = block.project

            val preRequestHandler = block.preRequestHandler
            if (preRequestHandler != null) {
                children.add(create(preRequestHandler, "Pre request handler", AllIcons.Actions.Play_first))
            }

            val location = StringBuilder()
            location.append(StringUtil.notNullize(path, "<not defined>"))

            val tabName: String
            val method = request.method
            tabName = getTabName(method)

            var icon = AllIcons.Actions.Annotate
            when (method.text) {
                HttpRequestEnum.GET.name -> {
                    icon = HttpIcons.GET
                }

                HttpRequestEnum.POST.name -> {
                    icon = HttpIcons.POST
                }

                HttpRequestEnum.PUT.name -> {
                    icon = HttpIcons.PUT
                }

                HttpRequestEnum.DELETE.name -> {
                    icon = HttpIcons.DELETE
                }
            }

            children.add(
                create(request, tabName, location.toString(), icon, StringUtil.isNotEmpty(originalHost))
            )

            val body = request.body
            val messagesGroup = body?.requestMessagesGroup
            if (messagesGroup != null) {
                var mimeType = "<not defined>"
                val contentType = request.contentType
                if (contentType != null) {
                    mimeType = contentType.mimeType
                }

                val bodyIcon = if (isImageType(contentType)) {
                    HttpIcons.IMAGE
                } else {
                    getInjectedLanguageIcon(project, messagesGroup.messageBody)
                }

                children.add(create(messagesGroup, "Request body $mimeType", bodyIcon))
            }

            val multipartMessage = body?.multipartMessage
            multipartMessage?.multipartFieldList?.forEach {
                var name = ""
                val headerFieldValue = HttpPsiImplUtil.getMultipartFieldDescription(it)
                if (headerFieldValue != null) {
                    name = getHeaderFieldOption(headerFieldValue, "name") ?: ""
                }

                var mimeType = "<not defined>"
                val contentType = it.contentType
                if (contentType != null) {
                    mimeType = contentType.mimeType
                }

                val multiIcon = if (isImageType(contentType)) {
                    HttpIcons.IMAGE
                } else {
                    getInjectedLanguageIcon(project, it.requestMessagesGroup.messageBody)
                }

                children.add(create(it, "Multipart field: $name $mimeType", multiIcon))
            }

            val responseHandler = request.responseHandler
            if (responseHandler != null) {
                children.add(create(responseHandler, "Response handler", AllIcons.Actions.Play_last))
            }

            return children
        }

        private fun isImageType(contentType: ContentType?): Boolean {
            if (contentType == null) {
                return false
            }

            return contentType == ContentType.IMAGE_PNG || contentType == ContentType.IMAGE_JPEG
                    || contentType == ContentType.IMAGE_BMP || contentType == ContentType.IMAGE_WEBP
                    || contentType == ContentType.IMAGE_SVG || contentType == ContentType.IMAGE_GIF
                    || contentType == ContentType.IMAGE_TIFF
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
