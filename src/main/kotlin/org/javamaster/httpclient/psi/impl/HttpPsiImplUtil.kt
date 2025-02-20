package org.javamaster.httpclient.psi.impl

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.psi.*
import java.net.http.HttpClient.Version

/**
 * @author yudong
 */
object HttpPsiImplUtil {

    @JvmStatic
    fun getVersion(httpVersion: HttpVersion): Version {
        val text = httpVersion.text
        return if (text.contains("1.1")) {
            Version.HTTP_1_1
        } else {
            Version.HTTP_2
        }
    }

    @JvmStatic
    fun getName(httpVariable: HttpVariable): String {
        val identifier = HttpPsiUtils.getNextSiblingByType(
            httpVariable.firstChild,
            HttpTypes.IDENTIFIER, false
        )
        return if (identifier != null) identifier.text else ""
    }

    @JvmStatic
    fun getUrl(httpRequestTarget: HttpRequestTarget): String {
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
                val split = text.split(";")
                split.forEach {
                    if (it.contains("boundary")) {
                        return text.split("=")[1]
                    }
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
    fun getHttpVersion(request: HttpRequest): Version {
        val psiElement =
            HttpPsiUtils.getNextSiblingByType(request.firstChild, HttpTypes.HTTP_VERSION, false)
                ?: return Version.HTTP_1_1
        val text = psiElement.text
        return if (text.contains("2.0")) {
            Version.HTTP_2
        } else {
            Version.HTTP_1_1
        }
    }

    @JvmStatic
    fun getHttpHost(request: HttpRequest): String {
        val target = request.requestTarget
        val host = target?.host
        if (host == null) {
            return target?.pathAbsolute?.text ?: ""
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

        val headerFieldValue = first.headerFieldValue?.firstChild?.text ?: return null
        val value = headerFieldValue.split(";")[0]
        return ContentType.getByMimeType(value)
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