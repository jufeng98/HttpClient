package org.javamaster.httpclient.doc

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.tree.TokenSet
import com.intellij.util.SmartList
import org.javamaster.httpclient.HttpPsiFactory.createDummyFile
import org.javamaster.httpclient.completion.support.HttpHeadersDictionary
import org.javamaster.httpclient.doc.support.HttpHeaderDocumentation
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.*
import org.jetbrains.annotations.Nls

/**
 * @author yudong
 */
class HttpHeadersDocumentationProvider : DocumentationProvider {
    private val headerSet = TokenSet.create(HttpTypes.FIELD_NAME, HttpTypes.FIELD_VALUE)

    override fun getUrlFor(element: PsiElement, originalElement: PsiElement): List<String>? {
        val doc = getDocumentation(element) ?: return null

        return SmartList(doc.url)
    }

    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): @Nls String? {
        val doc = getDocumentation(element) ?: return null

        return doc.generateDoc()
    }

    override fun getDocumentationElementForLookupItem(
        psiManager: PsiManager?,
        `object`: Any,
        element: PsiElement,
    ): PsiElement? {
        if (`object` !is HttpHeaderDocumentation) {
            return null
        }

        val name = `object`.name
        val project = psiManager?.project ?: return null

        if (StringUtil.isEmpty(name)) {
            return element
        }

        val file = createDummyFile(project, "GET http://127.0.0.1\n$name : ")
        val newRequest = file.getRequestBlocks()[0].request

        return newRequest.header!!.headerFieldList[0]
    }

    override fun getCustomDocumentationElement(
        editor: Editor,
        file: PsiFile,
        contextElement: PsiElement?,
        targetOffset: Int,
    ): PsiElement? {
        var psiElement = contextElement ?: return null

        if (file !is HttpFile) return null

        while (psiElement is PsiWhiteSpace || HttpPsiUtils.isOfType(psiElement, HttpTypes.COLON)) {
            psiElement = psiElement.prevSibling ?: return null
        }

        if (HttpPsiUtils.isOfTypes(psiElement, headerSet)) {
            psiElement = psiElement.parent
        }

        if (psiElement is HttpHeaderFieldName || psiElement is HttpHeaderFieldValue) {
            return psiElement.parent
        }

        return null
    }

    private fun getDocumentation(element: PsiElement): HttpHeaderDocumentation? {
        if (element !is HttpHeaderField) return null

        val name = element.name

        return HttpHeadersDictionary.getDocumentation(name)
    }
}