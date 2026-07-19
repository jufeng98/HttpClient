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
import com.intellij.psi.*
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.testFramework.LightVirtualFile
import org.apache.http.entity.ContentType
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

        if (httpMethod == HttpMethod.GET) {
            val queryParams = generateQueryParams(psiMethod)
            if (queryParams.size > 6) {
                val longParams = queryParams.joinToString("&\n") { "    " + it.first + "=" + it.second }
                val httpContent = """
### $methodDesc
GET {{baseUrl}}${request.path}?
$longParams
        """.trimIndent()

                val lightVirtualFile = LightVirtualFile("TemporaryHttpFile.http", HttpFileType.INSTANCE, httpContent)

                FileEditorManager.getInstance(project).openFile(lightVirtualFile, true)

                return
            }
        }

        var contentType: String
        var body: String
        var pathParams: String


        val jsonBody = generateJsonBody(psiMethod)
        if (jsonBody != null) {
            contentType = ContentType.APPLICATION_JSON.mimeType
            body = jsonBody
            val queryParams = generateQueryParams(psiMethod)
            pathParams = if (queryParams.isNotEmpty()) {
                "?" + queryParams.joinToString("&") { it.first + "=" + it.second }
            } else {
                ""
            }
        } else {
            contentType = ContentType.APPLICATION_FORM_URLENCODED.mimeType
            val queryParams = generateQueryParams(psiMethod)
            val noBody = httpMethod == HttpMethod.GET || httpMethod == HttpMethod.HEAD
                    || httpMethod == HttpMethod.OPTIONS
            if (noBody) {
                body = ""
                pathParams = if (queryParams.isNotEmpty()) {
                    "?" + queryParams.joinToString("&") { it.first + "=" + it.second }
                } else {
                    ""
                }
            } else {
                body = if (queryParams.isNotEmpty()) {
                    queryParams.joinToString(" &\n") { it.first + " = " + it.second }
                } else {
                    ""
                }
                pathParams = ""
            }
        }

        val httpContent = """
### $methodDesc
${httpMethod.name} {{baseUrl}}${request.path}${pathParams}
Accept: application/json
Content-Type: $contentType

$body
        """.trimIndent()

        val lightVirtualFile = LightVirtualFile("TemporaryHttpFile.http", HttpFileType.INSTANCE, httpContent)

        FileEditorManager.getInstance(project).openFile(lightVirtualFile, true)
    }

    private fun generateJsonBody(psiMethod: PsiMethod): String? {
        for (parameter in psiMethod.parameterList.parameters) {
            parameter.getAnnotation(REQUEST_BODY_ANNO_NAME) ?: continue

            val psiClass = PsiTypeUtils.resolvePsiType(parameter.type) ?: continue

            val map = convertToMap(psiClass)

            val superClass = psiClass.superClass
            if (superClass?.qualifiedName?.startsWith("java") == false) {
                val superMap = convertToMap(superClass)

                map.putAll(superMap)
            }

            return gson.toJson(map)
        }

        return null
    }

    private fun generateQueryParams(psiMethod: PsiMethod): MutableList<Pair<String, Any>> {
        val list = mutableListOf<Pair<String, Any>>()
        for (parameter in psiMethod.parameterList.parameters) {
            val psiAnnotation = parameter.getAnnotation(REQUEST_BODY_ANNO_NAME)
            if (psiAnnotation != null) {
                continue
            }

            val psiType = parameter.type
            if (psiType is PsiPrimitiveType) {
                list.add(Pair(parameter.name, getUrlTypeDefault(psiType)))
                continue
            }

            val psiClass = PsiTypeUtils.resolvePsiType(psiType) ?: continue

            val name = parameter.name
            if (psiClass.qualifiedName?.startsWith("java") == true) {
                list.add(Pair(name, getUrlTypeDefault(psiType)))
                continue
            }

            psiClass.fields.forEach {
                if (it.modifierList?.hasModifierProperty("static") == true) {
                    return@forEach
                }

                list.add(Pair(it.name, getUrlTypeDefault(it.type)))
            }

            val superClass = psiClass.superClass

            if (superClass?.qualifiedName?.startsWith("java") == false) {
                superClass.fields.forEach {
                    if (it.modifierList?.hasModifierProperty("static") == true) {
                        return@forEach
                    }

                    list.add(Pair(it.name, getUrlTypeDefault(it.type)))
                }
            }
        }

        return list
    }

    private fun getTypeDefault(type: PsiType): Any {
        val name = type.toString()

        val isCollection = InheritanceUtil.isInheritor(type, "java.util.Collection")
        if (isCollection) {
            val typeParameter = PsiUtil.extractIterableTypeParameter(type, false)

            val typePsiClass = PsiTypeUtils.resolvePsiType(typeParameter)

            val map = convertToMap(typePsiClass)

            return Lists.newArrayList<Any>(map)
        } else if (name.contains("Boolean") || name.contains("boolean")) {
            return false
        } else if (name.contains("Integer") || name.contains("int") || name.contains("Long") || name.contains("long")) {
            return 0
        } else if (name.contains("Double") || name.contains("double") || name.contains("Float") || name.contains("float")) {
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
        } else if (name.contains("Boolean") || name.contains("boolean")) {
            false
        } else if (name.contains("Integer") || name.contains("int") || name.contains("Long") || name.contains("long")) {
            0
        } else if (name.contains("Double") || name.contains("double") || name.contains("Float") || name.contains("float")) {
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
