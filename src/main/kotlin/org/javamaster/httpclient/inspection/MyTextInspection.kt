package org.javamaster.httpclient.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiFile
import org.javamaster.httpclient.inspection.support.InspectionHelper

/**
 * @author yudong
 */
class MyTextInspection : LocalInspectionTool() {

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
        return InspectionHelper.checkVariables(listOf(file), manager)
    }

}
