package org.javamaster.httpclient.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.inspection.support.InspectionHelper
import org.javamaster.httpclient.psi.HttpVariable

/**
 * @author yudong
 */
class HttpInspection : LocalInspectionTool() {

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
        val variables = PsiTreeUtil.findChildrenOfType(file, HttpVariable::class.java)

        return InspectionHelper.checkVariables(variables, manager)
    }

}
