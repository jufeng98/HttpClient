package org.javamaster.httpclient.psi.impl

import com.google.common.net.HttpHeaders
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.tree.IElementType
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.factory.HttpPsiFactory
import org.javamaster.httpclient.psi.*
import org.javamaster.httpclient.psi.HttpPsiUtils.getNextSiblingByType
import org.javamaster.httpclient.utils.DubboUtils
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
    fun getHttpVersion(request: HttpRequest): Version {
        val version = request.version ?: return Version.HTTP_1_1

        return getVersion(version)
    }

    @JvmStatic
    fun getName(variableName: HttpVariableName): String {
        return variableName.text
    }

    @JvmStatic
    fun getName(variableName: HttpGlobalVariableName): String {
        return getNextSiblingByType(variableName.firstChild, HttpTypes.GLOBAL_NAME, false)?.text ?: ""
    }

    @JvmStatic
    fun setName(variableName: HttpGlobalVariableName, name: String): PsiElement {
        val globalVariableName = HttpPsiFactory.createGlobalVariableName(variableName.project, "@$name =")
        return variableName.replace(globalVariableName)
    }

    @JvmStatic
    fun getNameIdentifier(variableName: HttpGlobalVariableName): PsiElement {
        val firstChild = variableName.firstChild
        return getNextSiblingByType(firstChild, HttpTypes.GLOBAL_NAME, false) ?: firstChild
    }

    @JvmStatic
    fun getReferences(variableName: HttpGlobalVariableName): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(variableName)
    }

    @JvmStatic
    fun getReferences(element: HttpQueryParameterKey): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(element)
    }

    @JvmStatic
    fun isBuiltin(variableName: HttpVariableName): Boolean {
        return variableName.variableBuiltin != null
    }

    @JvmStatic
    fun getName(headerField: HttpHeaderField): String {
        return headerField.headerFieldName.text
    }

    @JvmStatic
    fun getValue(headerField: HttpHeaderField): String? {
        return headerField.headerFieldValue?.text
    }

    @JvmStatic
    fun getUrl(httpRequestTarget: HttpRequestTarget): String {
        return httpRequestTarget.text
    }

    @JvmStatic
    fun getContentType(request: HttpRequest): ContentType? {
        return getContentType(request.header?.headerFieldList)
    }

    @JvmStatic
    fun getContentTypeField(header: HttpHeader): HttpHeaderField? {
        return header.headerFieldList
            .firstOrNull {
                it.headerFieldName.text.equals(HttpHeaders.CONTENT_TYPE, ignoreCase = true)
            }
    }

    @JvmStatic
    fun getInterfaceField(header: HttpHeader): HttpHeaderField? {
        return header.headerFieldList
            .firstOrNull {
                it.headerFieldName.text.equals(DubboUtils.INTERFACE_KEY, ignoreCase = true)
            }
    }

    @JvmStatic
    fun getContentTypeBoundary(request: HttpRequest): String? {
        val first = request.header?.headerFieldList
            ?.firstOrNull {
                it.headerFieldName.text.equals(HttpHeaders.CONTENT_TYPE, ignoreCase = true)
            }

        if (first == null) {
            return null
        }

        val headerFieldValue = first.headerFieldValue ?: return null

        return getHeaderFieldOption(headerFieldValue, "boundary")
    }

    @JvmStatic
    fun getContentDispositionName(header: HttpHeader): String? {
        val first = header.headerFieldList
            .firstOrNull {
                it.headerFieldName.text.equals(HttpHeaders.CONTENT_DISPOSITION, ignoreCase = true)
            }

        if (first == null) {
            return null
        }

        val headerFieldValue = first.headerFieldValue ?: return null

        return getHeaderFieldOption(headerFieldValue, "name")
    }

    @JvmStatic
    fun getContentDispositionFileName(header: HttpHeader): String? {
        val first = header.headerFieldList
            .firstOrNull {
                it.headerFieldName.text.equals(HttpHeaders.CONTENT_DISPOSITION, ignoreCase = true)
            }

        if (first == null) {
            return null
        }

        val headerFieldValue = first.headerFieldValue ?: return null

        return getHeaderFieldOption(headerFieldValue, "filename")
    }

    @JvmStatic
    fun getContentLength(request: HttpRequest): Int? {
        val first = request.header?.headerFieldList
            ?.firstOrNull {
                it.headerFieldName.text.equals(HttpHeaders.CONTENT_LENGTH, ignoreCase = true)
            }

        if (first == null) {
            return null
        }

        val text = first.headerFieldValue?.text ?: return null
        return text.toInt()
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
    fun getContentType(headerFieldList: List<HttpHeaderField>?): ContentType? {
        val first = headerFieldList
            ?.firstOrNull {
                it.headerFieldName.text.equals(HttpHeaders.CONTENT_TYPE, ignoreCase = true)
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
        return getContentType(request.header.headerFieldList)
    }

    @JvmStatic
    fun getReferences(param: HttpDirectionValue): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(param)
    }

    @JvmStatic
    fun getReferences(param: HttpRequestTarget): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(param)
    }

    @JvmStatic
    fun getReferences(param: HttpVariableName): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(param)
    }

    @JvmStatic
    fun getReferences(param: HttpVariableArg): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(param)
    }

    @JvmStatic
    fun getValue(param: HttpVariableArg): Any {
        val integer = param.integer
        if (integer != null) {
            return integer.text.toInt()
        } else {
            val text = param.string!!.text
            return text.substring(1, text.length - 1)
        }
    }

    @JvmStatic
    fun toArgsList(param: HttpVariableArgs): Array<Any> {
        return param.variableArgList
            .map {
                getValue(it)
            }
            .toTypedArray()
    }

    @JvmStatic
    fun getReferences(param: HttpFilePath): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(param)
    }

    @JvmStatic
    fun getMultipartFieldDescription(part: HttpMultipartField): HttpHeaderFieldValue? {
        val description = part.header.headerFieldList
            .firstOrNull { it.headerFieldName.text.equals(HttpHeaders.CONTENT_DISPOSITION, ignoreCase = true) }
        return description?.headerFieldValue
    }

    @JvmStatic
    fun getHeaderFieldOption(value: HttpHeaderFieldValue, optionName: String): String? {
        var child = value.firstChild
        while (child != null) {
            if (isOfType(child, HttpTypes.FIELD_VALUE)) {
                val text = child.text
                val split = text.split(";")
                split.forEach {
                    val tmp = it.trim()
                    if (tmp.startsWith(optionName)) {
                        return StringUtil.unquoteString(tmp.split("=")[1])
                    }
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