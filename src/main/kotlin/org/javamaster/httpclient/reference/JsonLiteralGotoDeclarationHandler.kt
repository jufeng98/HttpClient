package org.javamaster.httpclient.reference

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPlainTextFile
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpVariableName
import org.javamaster.httpclient.reference.support.HttpVariableNamePsiReference
import org.javamaster.httpclient.reference.support.TextVariableNamePsiReference

/**
 * @author yudong
 */
class JsonLiteralGotoDeclarationHandler : GotoDeclarationHandler {

    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor?,
    ): Array<PsiElement> {
        if (sourceElement == null) {
            return arrayOf()
        }

        val topFile = InjectedLanguageManager.getInstance(sourceElement.project).getTopLevelFile(sourceElement)
        if (topFile !is HttpFile) {
            return arrayOf()
        }

        val parent = sourceElement.parent
        if (parent is PsiPlainTextFile) {
            val elements = parent.references
                .mapNotNull {
                    if (it !is TextVariableNamePsiReference) {
                        return@mapNotNull null
                    }

                    it.multiResolve(false).map { it.element!! }
                }
                .flatten()
                .toList()
                .toTypedArray()

            if (elements.size > 1) {
                return elements
            }

            return arrayOf()
        }

        val parentParent = parent?.parent
        if (parentParent is HttpVariableName) {
            val elements = parentParent.references
                .mapNotNull {
                    if (it !is HttpVariableNamePsiReference) {
                        return@mapNotNull null
                    }

                    it.multiResolve(false).map { it.element!! }
                }
                .flatten()
                .toList()
                .toTypedArray()

            if (elements.size > 1) {
                return elements
            }

            return arrayOf()
        }

        return arrayOf()
    }

}
