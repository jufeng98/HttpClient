package org.javamaster.httpclient.liveTemplates

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiUtilCore
import org.javamaster.httpclient.HttpLanguage
import org.javamaster.httpclient.highlighting.support.HttpSyntaxHighlighter
import org.javamaster.httpclient.psi.HttpPsiUtils
import org.javamaster.httpclient.psi.HttpTypes

class HttpTemplateContextType : TemplateContextType("Http request") {
    override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
        return inContext(templateActionContext.file, templateActionContext.startOffset)
    }

    private fun inContext(file: PsiFile, offset: Int): Boolean {
        if (!PsiUtilCore.getLanguageAtOffset(file, offset).isKindOf(HttpLanguage.INSTANCE)) {
            return false
        }

        var element = file.findElementAt(offset)
        if (element is PsiWhiteSpace && offset > 0) {
            element = file.findElementAt(offset - 1)
        }

        return element != null && inContext(element)
    }

    private fun inContext(element: PsiElement): Boolean {
        return HttpPsiUtils.isOfType(element, HttpTypes.REQUEST_METHOD)
    }

    override fun createHighlighter(): SyntaxHighlighter {
        return HttpSyntaxHighlighter()
    }
}
