package org.javamaster.httpclient.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.inspection.support.InspectionHelper

class MyJsonInspection : LocalInspectionTool() {

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
        val jsonStringLiterals = PsiTreeUtil.findChildrenOfType(file, JsonStringLiteral::class.java)

        return InspectionHelper.checkVariables(jsonStringLiterals, manager)
    }
}
