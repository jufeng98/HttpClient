package org.javamaster.httpclient.scan.support

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.impl.PsiTreeChangeEventImpl
import com.intellij.psi.impl.PsiTreeChangePreprocessor
import org.javamaster.httpclient.enums.Control
import org.javamaster.httpclient.logger.logWarn
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class ControllerPsiTreeChangePreprocessor : PsiTreeChangePreprocessor, Disposable {
    private val controllerAnnoSet = setOf(
        Control.RestController.qualifiedName,
        Control.Controller.qualifiedName,
    )

    private val executor = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "ControllerPsiTreeChangePreprocessor").apply { isDaemon = true }
    }
    private var scheduledFuture: ScheduledFuture<*>? = null

    override fun treeChanged(event: PsiTreeChangeEventImpl) {
        val psiFile = event.file as? PsiJavaFile ?: return

        val code = event.code
        if (code == PsiTreeChangeEventImpl.PsiEventType.BEFORE_PROPERTY_CHANGE
            || code == PsiTreeChangeEventImpl.PsiEventType.PROPERTY_CHANGED
        ) {
            return
        }

        scheduledFuture?.cancel(false)

        scheduledFuture = executor.schedule({ scheduleControllerCheck(psiFile) }, 3, TimeUnit.SECONDS)
    }

    private fun scheduleControllerCheck(psiFile: PsiJavaFile) {
        val project = psiFile.project
        val controllerPsiModificationTracker = project.getService(ControllerPsiModificationTracker::class.java)
        val dumbService = DumbService.getInstance(project)

        dumbService.runWhenSmart {
            ReadAction
                .nonBlocking<Unit> {
                    if (isSpringController(psiFile)) {
                        controllerPsiModificationTracker.myModificationCount.incModificationCount()
                    }
                }
                .expireWhen { !psiFile.isValid }
                .submit(executor)
                .onError { logWarn("check controller failed", it) }
        }
    }

    private fun isSpringController(psiFile: PsiJavaFile): Boolean {
        for (psiClass in psiFile.classes) {
            if (checkClassForController(psiClass)) {
                return true
            }
        }
        return false
    }

    private fun checkClassForController(psiClass: PsiClass): Boolean {
        if (hasControllerAnnotationDirectly(psiClass)) {
            return true
        }

        if (hasControllerAnnotationInHierarchy(psiClass, 0)) {
            return true
        }

        return false
    }

    private fun hasControllerAnnotationDirectly(psiClass: PsiClass): Boolean {
        for (annotation in psiClass.annotations) {
            val qualifiedName = annotation.qualifiedName ?: continue

            if (controllerAnnoSet.contains(qualifiedName)) {
                return true
            }
        }

        return false
    }

    private fun hasControllerAnnotationInHierarchy(psiClass: PsiClass, depth: Int): Boolean {
        if (depth >= 3) {
            return false
        }

        val superClass = psiClass.superClass ?: return false

        if (hasControllerAnnotationDirectly(superClass)) {
            return true
        }

        return hasControllerAnnotationInHierarchy(superClass, depth + 1)
    }

    override fun dispose() {
        try {
            scheduledFuture?.cancel(false)
            executor.shutdown()
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow()
            }
        } catch (t: Throwable) {
            logWarn("dispose failed", t)
        }
    }

}