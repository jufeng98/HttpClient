package org.javamaster.httpclient.runconfig

import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import org.javamaster.httpclient.gutter.HttpGutterIconClickHandler
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.ui.HttpEditorTopForm
import org.javamaster.httpclient.utils.HttpUtils
import java.io.File

/**
 * @author yudong
 */
class HttpRunProfileState(
    val project: Project,
    private val environment: ExecutionEnvironment,
    private val env: String,
    private val httpFilePath: String,
) : RunProfileState {
    override fun execute(executor: Executor?, runner: ProgramRunner<*>): ExecutionResult? {
        val httpMethod = getTargetHttpMethod(httpFilePath, environment.runProfile.name, project) ?: return null

        HttpEditorTopForm.setCurrentEditorSelectedEnv(httpFilePath, project, env)

        val handler = HttpGutterIconClickHandler(httpMethod)
        handler.doRequest(null, env)

        return null
    }

    companion object {
        fun getTargetHttpMethod(httpFilePath: String, runConfigName: String, project: Project): HttpMethod? {
            val virtualFile = VfsUtil.findFileByIoFile(File(httpFilePath), false) ?: return null

            val psiFile = PsiUtil.getPsiFile(project, virtualFile)
            val httpMethods = PsiTreeUtil.findChildrenOfType(psiFile, HttpMethod::class.java)

            return httpMethods.firstOrNull {
                val tabName = HttpUtils.getTabName(it)
                runConfigName == tabName
            }
        }
    }
}
