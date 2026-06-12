package org.javamaster.httpclient.scan.support

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.impl.PsiTreeChangeEventImpl
import com.intellij.psi.impl.PsiTreeChangePreprocessor
import com.intellij.util.concurrency.AppExecutorUtil
import org.javamaster.httpclient.logger.HttpRequestLogger.logWarn
import org.javamaster.httpclient.scan.ScanRequest

/**
 * @author yudong
 */
class ControllerPsiTreeChangePreprocessor : PsiTreeChangePreprocessor {

    override fun treeChanged(event: PsiTreeChangeEventImpl) {
        val psiJavaFile = event.file as? PsiJavaFile ?: return

        val project = psiJavaFile.project
        val dumbService = DumbService.getInstance(project)
        if (dumbService.isDumb) {
            return
        }

        val code = event.code
        if (code == PsiTreeChangeEventImpl.PsiEventType.CHILDREN_CHANGED) {
            val scanRequest = project.getService(ScanRequest::class.java)

            dumbService.runWhenSmart {
                ReadAction
                    .nonBlocking<Unit> {
                        val module = ModuleUtilCore.findModuleForFile(psiJavaFile) ?: return@nonBlocking

                        scanRequest.handleFileChange(psiJavaFile, module)
                    }
                    .expireWhen { !psiJavaFile.isValid }
                    .submit(AppExecutorUtil.getAppExecutorService())
                    .onError { logWarn("scanRequest failed", it) }
            }
        }
    }

}