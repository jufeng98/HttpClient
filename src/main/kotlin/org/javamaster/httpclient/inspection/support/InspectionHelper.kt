package org.javamaster.httpclient.inspection.support

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.inspection.fix.CreateEnvVariableQuickFix
import org.javamaster.httpclient.inspection.fix.CreateFileVariableQuickFix
import org.javamaster.httpclient.inspection.fix.CreateJsVariableQuickFix
import org.javamaster.httpclient.reference.support.HttpVariablePsiReference
import org.javamaster.httpclient.reference.support.JsonValueVariablePsiReference

object InspectionHelper {

    fun checkVariables(
        psiElements: MutableCollection<out PsiElement>,
        manager: InspectionManager,
    ): Array<ProblemDescriptor> {
        val list = mutableListOf<ProblemDescriptor>()

        for (element in psiElements) {
            for (reference in element.references) {
                val variableName = when (reference) {
                    is HttpVariablePsiReference -> {
                        reference.variableName
                    }

                    is JsonValueVariablePsiReference -> {
                        reference.variableName
                    }

                    else -> {
                        null
                    }
                }

                if (variableName == null) {
                    continue
                }

                val resolve = reference.resolve()

                if (resolve != null) continue

                val fixes = mutableListOf< LocalQuickFix>(
                    CreateEnvVariableQuickFix(false, variableName),
                    CreateEnvVariableQuickFix(true, variableName),
                )

                if (reference is HttpVariablePsiReference) {
                    fixes.add(CreateJsVariableQuickFix(true, variableName))
                    fixes.add(CreateJsVariableQuickFix(false, variableName))
                }

                fixes.add(CreateFileVariableQuickFix(variableName))

                val problem = manager.createProblemDescriptor(
                    element,
                    reference.rangeInElement,
                    "Variable $variableName unresolved",
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
