package org.javamaster.httpclient.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiFile
import org.javamaster.httpclient.inspection.support.InspectionHelper
import org.javamaster.httpclient.parser.HttpFile

/**
 * @author yudong
 */
class HttpTextInspection : LocalInspectionTool() {

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
        val topLevelFile = InjectedLanguageManager.getInstance(file.project).getTopLevelFile(file)
        if (topLevelFile !is HttpFile) {
            return emptyArray()
        }

        return InspectionHelper.checkVariables(listOf(file), manager)
    }

}
