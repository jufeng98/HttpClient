package org.javamaster.httpclient.factory

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.HttpFileType
import org.javamaster.httpclient.HttpLanguage
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.*
import kotlin.jvm.java

/**
 * @author yudong
 */
object HttpPsiFactory {

    fun createDirectionComment(project: Project, content: String): HttpDirectionComment {
        val psiFile = createDummyFile(project, content)
        return PsiTreeUtil.findChildOfType(psiFile, HttpDirectionComment::class.java)!!
    }

    fun createGlobalVariableName(project: Project, content: String): HttpFileVariableName {
        val psiFile = createDummyFile(project, content)
        return PsiTreeUtil.findChildOfType(psiFile, HttpFileVariableName::class.java)!!
    }

    fun createVariable(project: Project, content: String): HttpVariable {
        val psiFile = createDummyFile(project, content)
        return PsiTreeUtil.findChildOfType(psiFile, HttpVariable::class.java)!!
    }

    fun createFileVariable(variableName: String, variableValue: String, project: Project): HttpFileVariable {
        val txt = "@$variableName = $variableValue\n"

        val psiFileFactory = PsiFileFactory.getInstance(project)

        val tmpFile = psiFileFactory.createFileFromText("dummy.http", HttpLanguage.INSTANCE, txt) as HttpFile
        return PsiTreeUtil.findChildOfType(tmpFile, HttpFileVariable::class.java)!!
    }

    fun createMessageBody(project: Project, content: String): HttpMessageBody {
        val str = "POST https://www.example.com\n\n$content"
        val psiFile = createDummyFile(project, str)
        return PsiTreeUtil.findChildOfType(psiFile, HttpMessageBody::class.java)!!
    }

    fun createFilePath(project: Project, path: String): HttpFilePath {
        val str = "import $path"
        val psiFile = createDummyFile(project, str)
        return PsiTreeUtil.findChildOfType(psiFile, HttpFilePath::class.java)!!
    }

    fun createDirectionValue(project: Project, path: String): HttpDirectionValue {
        val str = "# @import $path"
        val psiFile = createDummyFile(project, str)
        return PsiTreeUtil.findChildOfType(psiFile, HttpDirectionValue::class.java)!!
    }

    fun createScriptBody(project: Project, content: String): HttpScriptBody {
        val str = """
<! {%
$content
%}
run a.http            
        """.trimIndent()
        val psiFile = createDummyFile(project, str)
        return PsiTreeUtil.findChildOfType(psiFile, HttpScriptBody::class.java)!!
    }

    fun createDummyFile(project: Project, content: String): HttpFile {
        val fileType = HttpFileType.INSTANCE
        val fileName = "dummy." + fileType.defaultExtension
        return PsiFileFactory.getInstance(project)
            .createFileFromText(fileName, fileType, content, System.currentTimeMillis(), false) as HttpFile
    }

}
