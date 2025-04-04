package org.javamaster.httpclient

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpVariable

/**
 * @author yudong
 */
object HttpPsiFactory {

    fun createVariable(project: Project, content: String): HttpVariable {
        val psiFile = createDummyFile(project, content)
        return PsiTreeUtil.findChildOfType(psiFile, HttpVariable::class.java)!!
    }

    private fun createDummyFile(project: Project, content: String): HttpFile {
        val fileType = HttpFileType.INSTANCE
        val fileName = "dummy." + fileType.defaultExtension
        return PsiFileFactory.getInstance(project)
            .createFileFromText(fileName, fileType, content, System.currentTimeMillis(), false) as HttpFile
    }

}
