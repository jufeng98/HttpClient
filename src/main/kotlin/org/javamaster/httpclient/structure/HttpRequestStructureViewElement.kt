package org.javamaster.httpclient.structure

import com.intellij.icons.AllIcons
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.navigation.ColoredItemPresentation
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.Iconable
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.ArrayUtil
import com.intellij.util.SmartList
import com.intellij.util.containers.ContainerUtil
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpHeaderField
import org.javamaster.httpclient.psi.HttpPsiUtils
import org.javamaster.httpclient.psi.HttpRequestBlock
import org.javamaster.httpclient.utils.HttpUtils.getTabName
import java.util.function.Consumer
import javax.swing.Icon

class HttpRequestStructureViewElement private constructor(
    element: PsiElement, private val myPresentationText: String, private val myLocation: String?,
    private val myIcon: Icon?, private val myIsValid: Boolean,
) : PsiTreeElementBase<PsiElement?>(element),
    ColoredItemPresentation {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element
        if (element is HttpFile) {
            val blocks = HttpPsiUtils.getRequestBlocks(element as PsiFile)
            if (ArrayUtil.isEmpty(blocks)) {
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
                val request = block.request
                val originalHost = request?.httpHost ?: continue
                val target = request.requestTarget
                val path = target?.url


                val preRequestHandler = block.preRequestHandler
                if (preRequestHandler != null) {
                    children.add(create(preRequestHandler, "Pre request handler", AllIcons.Actions.Play_first))
                }

                val location = StringBuilder()
                location.append(StringUtil.notNullize(path, "<not defined>"))

                var tabName: String
                val method = request.method
                tabName = getTabName(method)

                var icon = AllIcons.Actions.Annotate
                val type = method.text
                if (type == HttpRequestEnum.GET.name) {
                    icon = HttpIcons.GET
                } else if (type == HttpRequestEnum.POST.name) {
                    icon = HttpIcons.POST
                }

                children.add(
                    create(request, tabName, location.toString(), icon, StringUtil.isNotEmpty(originalHost))
                )

                val responseHandler = request.responseHandler
                if (responseHandler != null) {
                    children.add(create(responseHandler, "Response handler", AllIcons.Actions.Play_last))
                }
            }
            return children
        } else if (element is HttpRequestBlock) {
            return getChildren(element)
        }

        return emptyList()
    }

    override fun getLocationString(): String? {
        return this.myLocation
    }

    override fun isSearchInLocationString(): Boolean {
        return true
    }

    override fun getTextAttributesKey(): TextAttributesKey? {
        return if (this.myIsValid) null else CodeInsightColors.ERRORS_ATTRIBUTES
    }

    override fun getPresentableText(): String {
        return this.myPresentationText
    }

    override fun getIcon(open: Boolean): Icon? {
        return this.myIcon ?: super.getIcon(open)
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
                element, StringUtil.notNullize(text, "<not defined>"), location, icon,
                StringUtil.isNotEmpty(text)
            )
        }

        fun create(
            element: PsiElement, text: String,
            location: String?, icon: Icon, isValid: Boolean,
        ): StructureViewTreeElement {
            return HttpRequestStructureViewElement(element, text, location, icon, isValid)
        }

        private fun getChildren(element: HttpRequestBlock): List<StructureViewTreeElement> {
            val elements = SmartList<StructureViewTreeElement>()
            val preRequestHandler = element.preRequestHandler
            if (preRequestHandler != null) {
                elements.add(create(preRequestHandler, "Pre request handler", AllIcons.Actions.Play_first))
            }

            val request = element.request
            val headers = request?.headerFieldList ?: return emptyList()
            if (headers.isNotEmpty()) {
                headers.forEach(Consumer { header: HttpHeaderField ->
                    val headerElement = create(
                        header, header.headerFieldName.text,
                        header.headerFieldValue?.text, AllIcons.Json.Array
                    )
                    elements.add(headerElement)
                })
            }

            val messagesGroup = request.requestMessagesGroup
            if (messagesGroup != null) {
                var mimeType = "<not defined>"
                val contentType = request.contentType
                if (contentType != null) {
                    mimeType = contentType.mimeType
                }

                var icon = AllIcons.FileTypes.Any_type
                val messageBody = messagesGroup.messageBody
                if (messageBody != null) {
                    val injectedLanguageManager = InjectedLanguageManager.getInstance(element.project)
                    val files = injectedLanguageManager.getInjectedPsiFiles(messageBody)
                    val psiElement = files?.get(0)?.first
                    val psiFile = psiElement as PsiFile?
                    val fileIcon = psiFile?.getIcon(Iconable.ICON_FLAG_VISIBILITY)
                    if (fileIcon != null) {
                        icon = fileIcon
                    }
                }

                elements.add(create(messagesGroup, "Request body $mimeType", icon))
            }

            val responseHandler = request.responseHandler
            if (responseHandler != null) {
                elements.add(create(responseHandler, "Response handler", AllIcons.Actions.Play_last))
            }

            return if (elements.isEmpty()) ContainerUtil.emptyList() else elements
        }

    }
}
