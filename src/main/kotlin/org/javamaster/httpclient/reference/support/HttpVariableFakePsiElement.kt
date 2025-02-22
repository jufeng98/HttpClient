package org.javamaster.httpclient.reference.support

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.env.EnvFileService
import org.javamaster.httpclient.reference.support.HttpFakePsiElement.Companion.showTip
import org.javamaster.httpclient.ui.HttpEditorTopForm

/**
 * @author yudong
 */
class HttpVariableFakePsiElement(private val element: PsiElement, private val variableName: String) :
    ASTWrapperPsiElement(element.node) {

    override fun navigate(requestFocus: Boolean) {
        if (variableName.startsWith("$")) {
            return
        }

        val selectedEnv = HttpEditorTopForm.getCurrentEditorSelectedEnv(project) ?: "dev"

        val path = element.containingFile.virtualFile.parent.path

        val jsonLiteral = EnvFileService.getEnvEle(variableName, selectedEnv, path, project)
        if (jsonLiteral == null) {
            showTip("在环境文件中未能解析该变量", project)
            return
        }

        jsonLiteral.navigate(true)
    }

}
