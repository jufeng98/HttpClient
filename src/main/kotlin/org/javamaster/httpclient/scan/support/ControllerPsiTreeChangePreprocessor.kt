package org.javamaster.httpclient.scan.support

import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.impl.PsiTreeChangeEventImpl
import com.intellij.psi.impl.PsiTreeChangePreprocessor
import org.javamaster.httpclient.scan.ScanRequest

/**
 * @author yudong
 */
class ControllerPsiTreeChangePreprocessor : PsiTreeChangePreprocessor {

    override fun treeChanged(event: PsiTreeChangeEventImpl) {
        val psiJavaFile = event.file as? PsiJavaFile ?: return

        val code = event.code
        if (code != PsiTreeChangeEventImpl.PsiEventType.CHILDREN_CHANGED) return

        val module = ModuleUtilCore.findModuleForFile(psiJavaFile) ?: return

        val scanRequest = psiJavaFile.project.getService(ScanRequest::class.java)

        val modificationTracker = scanRequest.getModificationTracker(module)

        modificationTracker.incModificationCount()
    }

}