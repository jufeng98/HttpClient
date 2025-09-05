package org.javamaster.httpclient.inlay

import com.google.common.collect.Lists
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
import com.intellij.psi.PsiField
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.LightVirtualFile
import org.javamaster.httpclient.enums.HttpMethod
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.scan.ScanRequest
import org.javamaster.httpclient.scan.support.Request
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.REQUEST_BODY_ANNO_NAME
import org.javamaster.httpclient.utils.HttpUtils.gson
import org.javamaster.httpclient.utils.PsiTypeUtils

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

        object : Task.Backgroundable(project, NlsBundle.nls("creating.file"), true) {
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

                            return@runReadAction
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

        val contentType = pair.first
        val body = pair.second

        if (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT) {
            lightVirtualFile.writeText(
                """
### $methodDesc
${httpMethod.name} {{baseUrl}}${request.path}
Accept: application/json
Content-Type: $contentType
            
$body
                """.trimIndent()
            )
        } else {
            val content = if (body.isEmpty()) "" else "?$body"

            lightVirtualFile.writeText(
                """
### $methodDesc
${httpMethod.name} {{baseUrl}}${request.path}$content
Accept: application/json
Content-Type: $contentType
                                                   
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
            val psiClass = PsiTypeUtils.resolvePsiType(parameter.type)!!

            val map = mutableMapOf<String, Any>()
            psiClass.fields.forEach {
                if (it.modifierList?.hasModifierProperty("static") == true) {
                    return@forEach
                }

                map[it.name] = getTypeDefault(it)
            }

            val superClass = psiClass.superClass
            if (superClass?.qualifiedName?.startsWith("java") == false) {
                superClass.fields.forEach {
                    if (it.modifierList?.hasModifierProperty("static") == true) {
                        return@forEach
                    }

                    map[it.name] = getTypeDefault(it)
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
            val psiClass = PsiTypeUtils.resolvePsiType(parameter.type)
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

        body = if (list.isEmpty()) "" else list.joinToString("&")

        return Pair("application/x-www-form-urlencoded", body)
    }

    private fun getTypeDefault(field: PsiField): Any {
        val type = field.type
        val name = type.toString()

        val isCollection = InheritanceUtil.isInheritor(type, "java.util.Collection")
        if (isCollection) {
            return Lists.newArrayList<String>()
        } else if (name.contains("Boolean")) {
            return false
        } else if (name.contains("Integer") || name.contains("int") || name.contains("Long") || name.contains("long")) {
            return 0
        } else if (name.contains("Double") || name.contains("double")) {
            return 0.0
        }

        val psiClass = PsiTypeUtils.resolvePsiType(type)
        if (psiClass?.qualifiedName?.startsWith("java") == false) {
            return Any()
        }

        return ""
    }

}
