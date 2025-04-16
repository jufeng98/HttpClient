package org.javamaster.httpclient.inspection.support

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.inspection.fix.CreateEnvVariableQuickFix
import org.javamaster.httpclient.inspection.fix.CreateFileVariableQuickFix
import org.javamaster.httpclient.inspection.fix.CreateJsVariableQuickFix
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.reference.support.HttpVariableNamePsiReference
import org.javamaster.httpclient.reference.support.TextVariableNamePsiReference

object InspectionHelper {

    fun checkVariables(
        psiElements: Collection<PsiElement>,
        manager: InspectionManager,
    ): Array<ProblemDescriptor> {
        val list = mutableListOf<ProblemDescriptor>()

        for (element in psiElements) {
            for (reference in element.references) {
                val builtin: Boolean
                val textRange: TextRange?
                val variableName = when (reference) {
                    is HttpVariableNamePsiReference -> {
                        textRange = reference.textRange
                        builtin = reference.element.isBuiltin
                        reference.element.name
                    }

                    is TextVariableNamePsiReference -> {
                        textRange = reference.textRange.shiftLeft(element.textRange.startOffset)
                        builtin = reference.variable.variableName?.isBuiltin ?: true
                        reference.variable.variableName?.name
                    }

                    else -> {
                        builtin = true
                        textRange = null
                        null
                    }
                }

                if (variableName == null) {
                    continue
                }

                if (builtin) {
                    continue
                }

                if (textRange?.startOffset == textRange?.endOffset) {
                    continue
                }

                val resolve = reference.resolve()

                if (resolve != null) continue

                val fixes = mutableListOf<LocalQuickFix>(
                    CreateEnvVariableQuickFix(false, variableName, PriorityAction.Priority.TOP),
                    CreateEnvVariableQuickFix(true, variableName, PriorityAction.Priority.HIGH),
                )

                if (reference is HttpVariableNamePsiReference) {
                    fixes.add(CreateJsVariableQuickFix(true, variableName))
                    fixes.add(CreateJsVariableQuickFix(false, variableName))
                }

                fixes.add(CreateFileVariableQuickFix(variableName))

                val problem = manager.createProblemDescriptor(
                    element,
                    textRange,
                    NlsBundle.nls("variable.unresolved", variableName),
                    ProblemHighlightType.WARNING,
                    true,
                    *fixes.toTypedArray()
                )
                list.add(problem)

            }
        }

        return list.toTypedArray()
    }

}
