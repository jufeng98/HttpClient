package org.javamaster.httpclient.structure

import com.intellij.httpClient.http.request.HttpRequestPsiUtils
import com.intellij.httpClient.http.request.psi.HttpHeaderField
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.httpClient.http.request.psi.HttpRequestElementType
import com.intellij.httpClient.http.request.psi.HttpRequestElementTypes
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
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.utils.HttpUtils.getTabName
import java.util.function.Consumer
import javax.swing.Icon

class HttpRequestStructureViewElement private constructor(
    element: PsiElement, private val myPresentationText: String, private val myLocation: String?,
    private val myIcon: Icon?, private val myIsValid: Boolean,
) : PsiTreeElementBase<PsiElement?>(element),
    ColoredItemPresentation {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = this.element
        if (element is HttpFile) {
            val blocks = HttpRequestPsiUtils.getRequestBlocks(element as PsiFile)
            if (ArrayUtil.isEmpty(blocks)) {
                return ContainerUtil.emptyList()
            }

            val children: MutableList<StructureViewTreeElement> = ArrayList(blocks.size)
            for (block in blocks) {
                val request = block.request
                val originalHost = request.httpHost
                val target = request.requestTarget
                val path = target?.httpUrl

                val location = StringBuilder()
                location.append(StringUtil.notNullize(path, "<not defined>"))

                var tabName = "httpClient"
                val method = request.method
                if (method != null) {
                    tabName = getTabName(method)
                }

                var icon = AllIcons.Actions.Annotate
                val type = method?.text
                if (type == (HttpRequestElementTypes.GET as HttpRequestElementType).name) {
                    icon = HttpIcons.GET
                } else if (type == (HttpRequestElementTypes.POST as HttpRequestElementType).name) {
                    icon = HttpIcons.POST
                }

                children.add(
                    createRequest(request, tabName, location.toString(), icon, StringUtil.isNotEmpty(originalHost))
                )
            }
            return children
        } else if (element is HttpRequest) {
            return getChildren(element)
        }

        return ContainerUtil.emptyList()
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
        private const val GRAPHQL_QUERY = "query"
        private const val GRAPHQL_MUTATION = "mutation"
        private const val GRAPHQL_SUBSCRIPTION = "subscription"

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

        fun createRequest(
            element: PsiElement, text: String,
            location: String?, icon: Icon, isValid: Boolean,
        ): StructureViewTreeElement {
            return HttpRequestStructureViewElement(element, text, location, icon, isValid)
        }

        private fun getChildren(element: HttpRequest): List<StructureViewTreeElement> {
            val elements = SmartList<StructureViewTreeElement>()
            val preRequestHandler = element.preRequestHandler
            if (preRequestHandler != null) {
                elements.add(create(preRequestHandler, "Pre request handler", AllIcons.Actions.Play_first))
            }

            val headers = element.headerFieldList
            if (headers.isNotEmpty()) {
                headers.forEach(Consumer { header: HttpHeaderField ->
                    val headerElement = create(
                        header, header.headerFieldName.text,
                        header.headerFieldValue?.text, AllIcons.Json.Array
                    )
                    elements.add(headerElement)
                })
            }

            val messagesGroup = element.requestMessagesGroup
            if (messagesGroup != null) {
                var mimeType = "<not defined>"
                val contentType = element.contentType
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

                val method = element.method
                if (method != null && method.text.equals("GRAPHQL", ignoreCase = true)) {
                    val methodType = graphQlMethodType(messagesGroup.text)
                    if (methodType != null) {
                        elements.add(create(messagesGroup, methodType, icon))
                    }
                } else {
                    elements.add(create(messagesGroup, "Request body $mimeType", icon))
                }
            }

            val responseHandler = element.responseHandler
            if (responseHandler != null) {
                elements.add(create(responseHandler, "Response handler", AllIcons.Actions.Play_last))
            }

            return if (elements.isEmpty()) ContainerUtil.emptyList() else elements
        }

        private fun graphQlMethodType(body: String): String? {
            val trimmedBody = body.trim { it <= ' ' }
            return if (trimmedBody.startsWith(GRAPHQL_QUERY)) {
                GRAPHQL_QUERY
            } else if (trimmedBody.startsWith(GRAPHQL_MUTATION)) {
                GRAPHQL_MUTATION
            } else {
                if (trimmedBody.startsWith(GRAPHQL_SUBSCRIPTION)) GRAPHQL_SUBSCRIPTION else null
            }
        }
    }
}
