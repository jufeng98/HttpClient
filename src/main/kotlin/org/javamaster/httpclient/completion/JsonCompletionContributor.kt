package org.javamaster.httpclient.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.json.JsonElementTypes
import com.intellij.json.psi.*
import com.intellij.openapi.vfs.impl.http.HttpVirtualFile
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtil
import org.javamaster.httpclient.completion.provider.JsonEmptyBodyCompletionProvider
import org.javamaster.httpclient.completion.provider.JsonKeyCompletionProvider

class JsonCompletionContributor : CompletionContributor() {

    init {
        this.extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withParent(
                JsonStringLiteral::class.java
            ),
            JsonKeyCompletionProvider()
        )

        this.extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .afterLeaf(PlatformPatterns.psiElement(JsonElementTypes.L_CURLY))
                .beforeLeaf(PlatformPatterns.psiElement(JsonElementTypes.R_CURLY)),
            JsonEmptyBodyCompletionProvider()
        )
    }

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val psiElement = parameters.position

        val virtualFile = PsiUtil.getVirtualFile(psiElement) ?: return

        if (virtualFile !is HttpVirtualFile) {
            result
        }

        super.fillCompletionVariants(parameters, result)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun invokeAutoPopup(position: PsiElement, typeChar: Char): Boolean {
        val parent = position.parent
        val parent1 = parent.parent
        return parent is JsonObject && parent1 is JsonFile && typeChar != '{' && typeChar != '['
                || parent is JsonObject && parent1 is JsonProperty && typeChar != '{' && typeChar != '['
                || parent is JsonObject && parent1 is JsonArray && typeChar != '{' && typeChar != '['
    }
}
