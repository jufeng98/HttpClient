package org.javamaster.httpclient.inspection.support

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
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
                if (reference !is HttpVariablePsiReference && reference !is JsonValueVariablePsiReference) continue

                val resolve = reference.resolve()

                if (resolve != null) continue

                val problem = manager.createProblemDescriptor(
                    element,
                    reference.rangeInElement,
                    "Variable unresolved",
                    ProblemHighlightType.WARNING,
                    true,
                )

                list.add(problem)
            }
        }

        return list.toTypedArray()
    }
}
