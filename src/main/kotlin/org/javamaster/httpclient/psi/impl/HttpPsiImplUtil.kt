package org.javamaster.httpclient.psi.impl

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.psi.*
import java.net.http.HttpClient

/**
 * @author yudong
 */
object HttpPsiImplUtil {

    @JvmStatic
    fun getName(httpVariable: HttpVariable): String {
        val identifier = HttpRequestPsiUtils.getNextSiblingByType(
            httpVariable.firstChild,
            HttpTypes.IDENTIFIER, false
        )
        return if (identifier != null) identifier.text else ""
    }

    @JvmStatic
    fun getHttpUrl(httpRequestTarget: HttpRequestTarget): String {
        return httpRequestTarget.text
    }

    @JvmStatic
    fun getContentType(request: HttpRequest): ContentType? {
        return getContentType(request.headerFieldList)
    }

    @JvmStatic
    fun getContentTypeBoundary(request: HttpRequest): String? {
        val first = request.headerFieldList
            .firstOrNull {
                it.headerFieldName.text.lowercase() == "content-type"
            }

        if (first == null) {
            return null
        }

        var child = first.headerFieldValue?.firstChild
        while (child != null) {
            if (child.elementType == HttpTypes.FIELD_VALUE) {
                val text = child.text

                if (text.startsWith("boundary")) {
                    return text.split("=")[1]
                }
            }

            child = child.nextSibling
        }

        return null
    }

    @JvmStatic
    fun getContentLength(request: HttpRequest): Int? {
        val first = request.headerFieldList
            .firstOrNull {
                it.headerFieldName.text.lowercase() == "content-length"
            }

        if (first == null) {
            return null
        }

        val text = first.headerFieldValue?.text ?: return null
        return text.toInt()
    }

    @JvmStatic
    fun getHttpVersion(request: HttpRequest): HttpClient.Version {
        val psiElement =
            HttpRequestPsiUtils.getNextSiblingByType(request.firstChild, HttpTypes.HTTP, false)
                ?: return HttpClient.Version.HTTP_1_1
        val text = psiElement.text
        return if (text.contains("2.0")) {
            HttpClient.Version.HTTP_2
        } else {
            HttpClient.Version.HTTP_1_1
        }
    }

    @JvmStatic
    fun getHttpHost(request: HttpRequest): String {
        val target = request.requestTarget
        val host = target?.host
        if (host == null) {
            val field = request.headerFieldList.firstOrNull { it.headerFieldName.text == "Host" }
            return if (field != null) field.headerFieldValue?.text ?: "" else ""
        } else {
            val port = target.getPort()
            return host.text + (if (port != null) ":${port.text}" else "")
        }
    }

    @JvmStatic
    fun getContentType(headerFieldList: List<HttpHeaderField>): ContentType? {
        val first = headerFieldList
            .firstOrNull {
                it.headerFieldName.text.lowercase() == "content-type"
            }

        if (first == null) {
            return null
        }

        val headerFieldValue = first.headerFieldValue?.firstChild?.text
        return ContentType.getByMimeType(headerFieldValue)
    }

    @JvmStatic
    fun getContentType(request: HttpMultipartField): ContentType? {
        return getContentType(request.headerFieldList)
    }

    @JvmStatic
    fun getReferences(param: HttpRequestTarget): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(param)
    }

    @JvmStatic
    fun getReferences(param: HttpVariable): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(param)
    }

    @JvmStatic
    fun getReferences(param: HttpFilePath): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(param)
    }

    @JvmStatic
    fun getReferences(param: HttpOutputFilePath): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(param)
    }

    @JvmStatic
    fun getMultipartFieldDescription(part: HttpMultipartField): HttpHeaderFieldValue? {
        val description = part.headerFieldList
            .firstOrNull { it.headerFieldName.text == "Content-Disposition" }
        return description?.headerFieldValue
    }

    @JvmStatic
    fun getHeaderFieldOption(value: HttpHeaderFieldValue, optionName: String): String? {
        var child = value.firstChild
        while (child != null) {
            if (isOfType(child, HttpTypes.FIELD_VALUE)) {
                val option = child.text.trim { it <= ' ' }
                if (option.length > optionName.length + 1 && option.startsWith(optionName) && option[optionName.length] == '=') {
                    return StringUtil.unquoteString(option.substring(optionName.length + 1))
                }
            }
            child = child.nextSibling
        }

        return null
    }

    @JvmStatic
    fun isOfType(element: PsiElement?, type: IElementType): Boolean {
        if (element == null) {
            return false
        }

        val node = element.node
        return node != null && node.elementType === type
    }

    @JvmStatic
    fun getReferences(param: HttpHeaderFieldValue): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(param)
    }
}