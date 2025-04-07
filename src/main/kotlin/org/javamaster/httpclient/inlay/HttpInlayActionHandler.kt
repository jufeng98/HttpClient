package org.javamaster.httpclient.inlay

import com.intellij.codeInsight.hints.declarative.InlayActionHandler
import com.intellij.codeInsight.hints.declarative.InlayActionPayload
import com.intellij.codeInsight.hints.declarative.PsiPointerInlayActionPayload
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.writeText
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.LightVirtualFile
import org.javamaster.httpclient.scan.ScanRequest
import org.javamaster.httpclient.scan.support.Request
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class HttpInlayActionHandler : InlayActionHandler {

    override fun handleClick(e: EditorMouseEvent, payload: InlayActionPayload) {
        val actionPayload = payload as PsiPointerInlayActionPayload
        val element = actionPayload.pointer.element as PsiLiteralExpression
        val project = element.project

        object : Task.Backgroundable(project, "Creating temporary http file...", true) {
            override fun run(indicator: ProgressIndicator) {
                runReadAction {
                    val method = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)

                    val module = ModuleUtil.findModuleForPsiElement(element) ?: return@runReadAction

                    val map = ScanRequest.getCacheRequestMap(module, project)

                    val values = map.values
                    for (value in values) {
                        for (request in value) {
                            val psiMethod = request.psiElement ?: continue

                            if (method != psiMethod) {
                                continue
                            }

                            runInEdt {
                                runWriteAction {
                                    createRequest(project, request)
                                }
                            }
                            break
                        }
                    }
                }
            }
        }.queue()
    }

    private fun createRequest(project: Project, request: Request) {
        val methodDesc = HttpUtils.getMethodDesc(request.psiElement!!)

        val lightVirtualFile = LightVirtualFile("TemporaryHttpFile.http")
        lightVirtualFile.writeText(
            """
            ### $methodDesc
            ${request.method.name} http://localhost${request.path}
            Accept: application/json
            Content-Type: application/json
                        
            {
                "name": "yu"
            }
        """.trimIndent()
        )

        FileEditorManager.getInstance(project).openFile(lightVirtualFile, true)
    }
}
