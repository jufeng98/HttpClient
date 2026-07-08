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
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.testFramework.LightVirtualFile
import org.javamaster.httpclient.HttpFileType
import org.javamaster.httpclient.consts.HttpConsts.Companion.REQUEST_BODY_ANNO_NAME
import org.javamaster.httpclient.enums.HttpMethod
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.scan.ScanRequest
import org.javamaster.httpclient.scan.support.Request
import org.javamaster.httpclient.utils.JsonUtils.gson
import org.javamaster.httpclient.utils.MyPsiUtils
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
        val scanRequest = project.getService(ScanRequest::class.java)

        object : Task.Backgroundable(project, NlsBundle.nls("creating.file"), true) {
            override fun run(indicator: ProgressIndicator) {
                runReadAction {
                    val method = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java) ?: return@runReadAction

                    val qualifiedName = method.containingClass?.qualifiedName ?: return@runReadAction

                    val module = ModuleUtil.findModuleForPsiElement(element) ?: return@runReadAction

                    val map = scanRequest.getCacheRequestMap(module)

                    val requests = map[qualifiedName] ?: return@runReadAction

                    for (request in requests) {
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
        }.queue()
    }

    private fun createRequest(project: Project, request: Request) {
        val psiMethod = request.psiElement!!
        val methodDesc = MyPsiUtils.getMethodDesc(psiMethod)
        val httpMethod = request.method

        val pair = generateBody(psiMethod)

        val contentType = pair.first
        val body = pair.second

        val contentStr = if (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT) {
            """
### $methodDesc
${httpMethod.name} {{baseUrl}}${request.path}
Accept: application/json
Content-Type: $contentType
            
$body
            """.trimIndent()
        } else {
            val content = if (body.isEmpty()) "" else "?$body"

            """
### $methodDesc
${httpMethod.name} {{baseUrl}}${request.path}$content
Accept: application/json
Content-Type: $contentType
                                                   
            """.trimIndent()
        }

        val lightVirtualFile = LightVirtualFile("TemporaryHttpFile.http", HttpFileType.INSTANCE, contentStr)

        FileEditorManager.getInstance(project).openFile(lightVirtualFile, true)
    }

    private fun generateBody(psiMethod: PsiMethod): Pair<String, String> {
        var body = ""
        var hasAnno = false
        for (parameter in psiMethod.parameterList.parameters) {
            parameter.getAnnotation(REQUEST_BODY_ANNO_NAME) ?: continue

            hasAnno = true
            val psiClass = PsiTypeUtils.resolvePsiType(parameter.type)!!

            val map = convertToMap(psiClass)

            val superClass = psiClass.superClass
            if (superClass?.qualifiedName?.startsWith("java") == false) {
                val superMap = convertToMap(superClass)

                map.putAll(superMap)
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
                list.add("$name = " + getUrlTypeDefault(parameter.type))
            } else {
                psiClass?.fields?.forEach {
                    if (it.modifierList?.hasModifierProperty("static") == true) {
                        return@forEach
                    }

                    list.add("${it.name} = " + getUrlTypeDefault(it.type))
                }

                val superClass = psiClass?.superClass
                if (superClass?.qualifiedName?.startsWith("java") == false) {
                    superClass.fields.forEach {
                        if (it.modifierList?.hasModifierProperty("static") == true) {
                            return@forEach
                        }

                        list.add("${it.name} = " + getUrlTypeDefault(it.type))
                    }
                }
            }
        }

        body = if (list.isEmpty()) "" else list.joinToString(" &\n")

        return Pair("application/x-www-form-urlencoded", body)
    }

    private fun getTypeDefault(type: PsiType): Any {
        val name = type.toString()

        val isCollection = InheritanceUtil.isInheritor(type, "java.util.Collection")
        if (isCollection) {
            val typeParameter = PsiUtil.extractIterableTypeParameter(type, false)

            val typePsiClass = PsiTypeUtils.resolvePsiType(typeParameter)

            val map = convertToMap(typePsiClass)

            return Lists.newArrayList<Any>(map)
        } else if (name.contains("Boolean")) {
            return false
        } else if (name.contains("Integer") || name.contains("int") || name.contains("Long") || name.contains("long")) {
            return 0
        } else if (name.contains("Double") || name.contains("double")) {
            return 0.0
        } else if (name.contains("String")) {
            return ""
        }

        val psiClass = PsiTypeUtils.resolvePsiType(type)
        if (psiClass == null) {
            return ""
        }

        return if (psiClass.qualifiedName?.startsWith("java") == true) {
            Any()
        } else {
            convertToMap(psiClass)
        }
    }

    private fun getUrlTypeDefault(type: PsiType): Any {
        val name = type.toString()

        val isCollection = InheritanceUtil.isInheritor(type, "java.util.Collection")
        return if (isCollection) {
            ""
        } else if (name.contains("Boolean")) {
            false
        } else if (name.contains("Integer") || name.contains("int") || name.contains("Long") || name.contains("long")) {
            0
        } else if (name.contains("Double") || name.contains("double")) {
            0.0
        } else {
            ""
        }
    }

    private fun convertToMap(psiClass: PsiClass?): MutableMap<String, Any> {
        val map = mutableMapOf<String, Any>()

        if (psiClass == null) {
            return map
        }

        psiClass.fields.forEach {
            if (it.modifierList?.hasModifierProperty("static") == true) {
                return@forEach
            }

            map[it.name] = getTypeDefault(it.type)
        }

        return map
    }
}
