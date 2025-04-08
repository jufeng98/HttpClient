package org.javamaster.httpclient.inlay

import com.intellij.codeInsight.hints.declarative.InlayActionHandler
import com.intellij.codeInsight.hints.declarative.InlayActionPayload
import com.intellij.codeInsight.hints.declarative.PsiPointerInlayActionPayload
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
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
import org.javamaster.httpclient.enums.HttpMethod
import org.javamaster.httpclient.scan.ScanRequest
import org.javamaster.httpclient.scan.support.Request
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.REQUEST_BODY_ANNO_NAME
import org.javamaster.httpclient.utils.HttpUtils.gson
import org.javamaster.httpclient.utils.PsiUtils

/**
 * @author yudong
 */
class HttpInlayActionHandler : InlayActionHandler {

    @Suppress("OVERRIDE_DEPRECATION")
    override fun handleClick(editor: Editor, payload: InlayActionPayload) {
        throw UnsupportedOperationException()
    }

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
        val psiMethod = request.psiElement!!
        val methodDesc = HttpUtils.getMethodDesc(psiMethod)
        val httpMethod = request.method
        val lightVirtualFile = LightVirtualFile("TemporaryHttpFile.http")

        val pair = generateBody(psiMethod)

        if (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT) {
            lightVirtualFile.writeText(
                """
### $methodDesc
${httpMethod.name} http://localhost${request.path}
Accept: application/json
Content-Type: ${pair.first}
            
${pair.second}
                """.trimIndent()
            )
        } else {
            lightVirtualFile.writeText(
                """
### $methodDesc
${httpMethod.name} http://localhost${request.path}${pair.second}
Accept: application/json
Content-Type: ${pair.first}
                                                   
                """.trimIndent()
            )
        }

        FileEditorManager.getInstance(project).openFile(lightVirtualFile, true)
    }

    private fun generateBody(psiMethod: PsiMethod): Pair<String, String> {
        var body = ""
        var hasAnno = false
        for (parameter in psiMethod.parameterList.parameters) {
            parameter.getAnnotation(REQUEST_BODY_ANNO_NAME) ?: continue

            hasAnno = true
            val psiClass = PsiUtils.resolvePsiType(parameter.type)!!

            val map = mutableMapOf<String, String>()
            psiClass.fields.forEach {
                if (it.modifierList?.hasModifierProperty("static") == true) {
                    return@forEach
                }

                map[it.name] = ""
            }

            val superClass = psiClass.superClass
            if (superClass?.qualifiedName?.startsWith("java") == false) {
                superClass.fields.forEach {
                    if (it.modifierList?.hasModifierProperty("static") == true) {
                        return@forEach
                    }

                    map[it.name] = ""
                }
            }

            body = gson.toJson(map)
            break
        }

        if (hasAnno) {
            return Pair("application/json", body)
        }

        val list = mutableListOf<String>()
        for (parameter in psiMethod.parameterList.parameters) {
            val name = parameter.name
            val psiClass = PsiUtils.resolvePsiType(parameter.type)
            if (psiClass?.qualifiedName?.startsWith("java") == true) {
                list.add("$name=")
            } else {
                psiClass?.fields?.forEach {
                    if (it.modifierList?.hasModifierProperty("static") == true) {
                        return@forEach
                    }

                    list.add("${it.name}=")
                }

                val superClass = psiClass?.superClass
                if (superClass?.qualifiedName?.startsWith("java") == false) {
                    superClass.fields.forEach {
                        if (it.modifierList?.hasModifierProperty("static") == true) {
                            return@forEach
                        }

                        list.add("${it.name}=")
                    }
                }
            }
        }

        body = if (list.isEmpty()) "" else "?" + list.joinToString("&")

        return Pair("application/x-www-form-urlencoded", body)
    }
}
