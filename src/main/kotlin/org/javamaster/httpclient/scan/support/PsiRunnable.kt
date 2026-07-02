package org.javamaster.httpclient.scan.support

import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiJavaFile
import org.javamaster.httpclient.logger.HttpRequestLogger
import org.javamaster.httpclient.scan.ScanRequest
import org.javamaster.httpclient.utils.HttpUtils

internal class PsiRunnable(
    internal val psiJavaFile: PsiJavaFile,
    private val project: Project,
) : Runnable {

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }

        return psiJavaFile == (other as PsiRunnable).psiJavaFile
    }

    override fun hashCode(): Int {
        return psiJavaFile.virtualFile.path.hashCode()
    }

    override fun run() {
        try {
            if (!psiJavaFile.isValid) {
                return
            }

            val module = ModuleUtilCore.findModuleForFile(psiJavaFile) ?: return

            val scanRequest = project.getService(ScanRequest::class.java)

            HttpUtils.runReadAction {
                scanRequest.handleFileChange(psiJavaFile, module)
            }
        } catch (t: Throwable) {
            HttpRequestLogger.logWarn("处理路径错误:" + t.message)
        }
    }

}
