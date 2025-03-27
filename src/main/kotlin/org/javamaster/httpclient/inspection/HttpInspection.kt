package org.javamaster.httpclient.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.inspection.support.InspectionHelper
import org.javamaster.httpclient.psi.HttpVariableName

/**
 * @author yudong
 */
class HttpInspection : LocalInspectionTool() {

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
        val variables = mutableListOf<PsiElement>()

        PsiTreeUtil.processElements(file) {
            if (it is HttpVariableName) {
                variables.add(it)
            }

            true
        }

        return InspectionHelper.checkVariables(variables, manager)
    }

}
