package org.javamaster.httpclient.reference.support

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.env.EnvFileService
import org.javamaster.httpclient.reference.support.HttpFakePsiElement.Companion.showTip
import org.javamaster.httpclient.ui.HttpEditorTopForm
import org.javamaster.httpclient.utils.HttpUtils
import java.io.File

/**
 * @author yudong
 */
class HttpVariableFakePsiElement(private val element: PsiElement, private val variableName: String) :
    ASTWrapperPsiElement(element.node) {

    override fun navigate(requestFocus: Boolean) {
        if (variableName.startsWith(InnerVariableEnum.IMAGE_TO_BASE64.methodName)) {
            tryOpenParamFile()
            return
        }

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

    private fun tryOpenParamFile() {
        val editorManager = FileEditorManager.getInstance(project)
        val httpFileParentPath = editorManager.selectedEditor!!.file!!.parent!!.path

        val imagePath = variableName.substring(
            InnerVariableEnum.IMAGE_TO_BASE64.methodName.length + 1,
            variableName.length - 1
        )
        val filePath = HttpUtils.constructFilePath(imagePath, httpFileParentPath)
        val file = File(filePath)
        if (!file.exists()) {
            showTip("文件 ${file.normalize()} 不存在!", project)
            return
        }

        val virtualFile = VfsUtil.findFileByIoFile(file, true)!!
        val openFile = editorManager.openFile(virtualFile, true)

        if (openFile.isEmpty()) {
            // 无法打开文件,就跳转到其目录
            ProjectView.getInstance(project).select(null, virtualFile, true)
        }
    }

}
