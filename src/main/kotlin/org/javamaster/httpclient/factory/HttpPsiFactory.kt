package org.javamaster.httpclient.factory

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.HttpFileType
import org.javamaster.httpclient.HttpLanguage
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpGlobalVariable
import org.javamaster.httpclient.psi.HttpGlobalVariableName
import org.javamaster.httpclient.psi.HttpVariable

/**
 * @author yudong
 */
object HttpPsiFactory {

    fun createGlobalVariableName(project: Project, content: String): HttpGlobalVariableName {
        val psiFile = createDummyFile(project, content)
        return PsiTreeUtil.findChildOfType(psiFile, HttpGlobalVariableName::class.java)!!
    }

    fun createVariable(project: Project, content: String): HttpVariable {
        val psiFile = createDummyFile(project, content)
        return PsiTreeUtil.findChildOfType(psiFile, HttpVariable::class.java)!!
    }

    fun createGlobalVariable(variableName: String, variableValue: String, project: Project): HttpGlobalVariable {
        val txt = "@$variableName = $variableValue\n"

        val psiFileFactory = PsiFileFactory.getInstance(project)

        val tmpFile = psiFileFactory.createFileFromText("dummy.http", HttpLanguage.INSTANCE, txt) as HttpFile
        return PsiTreeUtil.findChildOfType(tmpFile, HttpGlobalVariable::class.java)!!
    }

    fun createDummyFile(project: Project, content: String): HttpFile {
        val fileType = HttpFileType.INSTANCE
        val fileName = "dummy." + fileType.defaultExtension
        return PsiFileFactory.getInstance(project)
            .createFileFromText(fileName, fileType, content, System.currentTimeMillis(), false) as HttpFile
    }

}
