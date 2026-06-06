package org.javamaster.httpclient.utils

import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.util.SmartList
import org.javamaster.httpclient.HttpFileType
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.consts.HttpConsts.Companion.API_MODEL_PROPERTY_ANNO_NAME
import org.javamaster.httpclient.consts.HttpConsts.Companion.API_OPERATION_ANNO_NAME
import org.javamaster.httpclient.consts.HttpConsts.Companion.REQUEST_BODY_ANNO_NAME
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.*
import org.javamaster.httpclient.psi.HttpPsiUtils.getNextSiblingByType
import org.javamaster.httpclient.utils.HttpUtils.collectJsonPropertyNameLevels
import org.javamaster.httpclient.utils.HttpUtils.resolveTargetField
import java.io.File
import java.net.http.HttpClient
import javax.swing.Icon

/**
 * @author yudong
 */
class MyPsiUtils {

    companion object {

        fun getImportFileHttpRequests(httpFile: HttpFile): List<Pair<HttpComment, HttpMethod>> {
            val project = httpFile.project
            val parentPath = httpFile.virtualFile.parent.path
            val globalImports = httpFile.getGlobalImports()
            return globalImports
                .mapNotNull {
                    val path = it.filePath?.text ?: return@mapNotNull null
                    val importHttpFilePath = HttpUtils.constructFilePath(path, parentPath)

                    getHttpRequests(importHttpFilePath, project)
                }
                .flatten()
        }

        private fun getHttpRequests(httpFilePath: String, project: Project): List<Pair<HttpComment, HttpMethod>> {
            val file = File(httpFilePath)
            if (file.extension != HttpFileType.DEFAULT_EXTENSION) {
                return listOf()
            }

            val importVirtualFile = HttpUtils.findVirtualFile(httpFilePath)
            if (importVirtualFile == null) {
                return listOf()
            }

            val httpFile = PsiUtil.getPsiFile(project, importVirtualFile) as HttpFile
            val requestBlocks =
                PsiTreeUtil.getChildrenOfTypeAsList(httpFile, HttpRequestBlock::class.java)
            return requestBlocks.mapNotNull { requestBlock ->
                var comment = requestBlock.comment ?: return@mapNotNull null

                val method = requestBlock.request?.method ?: return@mapNotNull null

                Pair(comment, method)
            }
        }

        fun getAllPreJsScripts(httpFile: PsiFile, httpRequestBlock: HttpRequestBlock): List<HttpScriptBody> {
            val scripts = mutableListOf<HttpScriptBody>()

            val globalScript = getGlobalJsScript(httpFile)
            if (globalScript != null) {
                scripts.add(globalScript)
            }

            val preJsScript = getPreJsScript(httpRequestBlock)
            if (preJsScript != null) {
                scripts.add(preJsScript)
            }

            return scripts
        }

        fun getAllPostJsScripts(httpFile: PsiFile): List<HttpScriptBody> {
            val handlers = PsiTreeUtil.findChildrenOfType(httpFile, HttpResponseHandler::class.java)

            return handlers
                .mapNotNull {
                    getJsScript(it)
                }
        }

        fun getJsScript(httpResponseHandler: HttpResponseHandler?): HttpScriptBody? {
            if (httpResponseHandler == null) {
                return null
            }

            return httpResponseHandler.responseScript.scriptBody
        }

        fun generateAnno(annotation: PsiAnnotation): String {
            val html = """
            <div class='definition'>
                <span style="color:#808000;">@</span><a href="psi_element://${annotation.qualifiedName}"><span style="color:#808000;">${annotation.nameReferenceElement?.text}</span></a><span>${annotation.parameterList.text}</span>
            </div>
        """.trimIndent()

            return html
        }

        private fun getGlobalJsScript(httpFile: PsiFile): HttpScriptBody? {
            val globalHandler = PsiTreeUtil.getChildOfType(httpFile, HttpGlobalHandler::class.java) ?: return null
            return globalHandler.globalScript.scriptBody
        }

        private fun getPreJsScript(httpRequestBlock: HttpRequestBlock): HttpScriptBody? {
            val preRequestHandler = httpRequestBlock.preRequestHandler ?: return null
            return preRequestHandler.preRequestScript.scriptBody
        }

        fun getReqDirectionCommentParamMap(httpRequestBlock: HttpRequestBlock): Map<String, String> {
            val map = mutableMapOf<String, String>()

            httpRequestBlock.directionCommentList
                .forEach {
                    val name = it.directionName?.text ?: return@forEach
                    map[name] = it.directionValue?.text ?: ""
                }

            return map
        }

        fun getMethodDesc(psiMethod: PsiMethod): String {
            val list = mutableListOf<String>()

            val docComment = psiMethod.docComment
            if (docComment != null) {
                val comment = getNextSiblingByType(docComment.firstChild, JavaDocTokenType.DOC_COMMENT_DATA, false)
                    ?.text?.trim()

                comment?.let { list.add(it) }
            }

            val annotation = psiMethod.getAnnotation(API_OPERATION_ANNO_NAME)
            if (annotation != null) {
                val attributeValue = annotation.findAttributeValue("value")
                if (attributeValue is PsiPolyadicExpression) {
                    attributeValue.operands
                        .filter { it is PsiLiteralExpression? }
                        .forEach {
                            val desc = (it as PsiLiteralExpression?)?.value?.toString()?.trim()
                            desc?.let { list.add(it) }
                        }
                } else if (attributeValue is PsiLiteralExpression?) {
                    val desc = attributeValue?.value?.toString()?.trim()
                    desc?.let { list.add(it) }
                }
            }

            return list.joinToString(" ")
        }

        fun getPsiFieldDesc(psiField: PsiField): String {
            val list = SmartList<String>()

            val docComment = psiField.docComment
            if (docComment != null) {
                val comment = getNextSiblingByType(docComment.firstChild, JavaDocTokenType.DOC_COMMENT_DATA, false)
                    ?.text?.trim()

                comment?.let { list.add(it) }
            }

            val annotation = psiField.getAnnotation(API_MODEL_PROPERTY_ANNO_NAME)
            if (annotation != null) {
                val attributeValue = annotation.findAttributeValue("value") as PsiLiteralExpression?

                val desc = attributeValue?.value?.toString()?.trim()

                desc?.let { list.add(it) }
            }

            return list.joinToString(" ")
        }

        fun getVersionDesc(version: HttpClient.Version): String {
            return if (version == HttpClient.Version.HTTP_1_1) {
                "HTTP/1.1"
            } else {
                "HTTP/2"
            }
        }

        fun pickMethodIcon(method: String): Icon {
            try {
                val methodType = HttpRequestEnum.getInstance(method)

                return methodType.icon
            } catch (_: UnsupportedOperationException) {
                return HttpIcons.FILE
            }
        }

        fun resolveUrlControllerTargetPsiClass(psiElement: PsiElement): PsiClass? {
            val jsonProperty = PsiTreeUtil.getParentOfType(psiElement, JsonProperty::class.java)
            val parentJsonProperty = PsiTreeUtil.getParentOfType(jsonProperty, JsonProperty::class.java)

            val noParentProperty = parentJsonProperty == null

            val jsonString = if (noParentProperty) {
                psiElement
            } else {
                PsiTreeUtil.getChildOfType(parentJsonProperty, JsonStringLiteral::class.java)!!
            }

            val controllerMethod = getUrlControllerMethod(jsonString, jsonString.project) ?: return null

            val paramPsiType = getUrlControllerMethodParamType(jsonString, controllerMethod)

            val paramPsiCls = PsiTypeUtils.resolvePsiType(paramPsiType) ?: return null

            if (noParentProperty) {
                return paramPsiCls
            }

            val classGenericParameters = (paramPsiType as PsiClassReferenceType).parameters

            val jsonPropertyNameLevels = collectJsonPropertyNameLevels(jsonString as JsonStringLiteral)

            val targetField =
                resolveTargetField(paramPsiCls, jsonPropertyNameLevels, classGenericParameters) ?: return null

            val psiType = targetField.type

            val psiClass = PsiTypeUtils.resolvePsiType(psiType)

            val isCollection = InheritanceUtil.isInheritor(psiClass, "java.util.Collection")
            return if (isCollection) {
                val parameters = (psiType as PsiClassReferenceType).parameters
                if (parameters.size > 0) {
                    PsiTypeUtils.resolvePsiType(parameters[0])
                } else {
                    null
                }
            } else {
                psiClass
            }
        }

        fun getUrlControllerMethodParamType(psiElement: PsiElement, controllerMethod: PsiMethod): PsiType? {
            val virtualFile = PsiUtil.getVirtualFile(psiElement)

            return if (virtualFile?.name?.endsWith("res.http") == true) {
                controllerMethod.returnType
            } else {
                resolveTargetParam(controllerMethod)?.type
            }
        }

        fun resolveTargetParam(psiMethod: PsiMethod): PsiParameter? {
            val superPsiMethods = psiMethod.findSuperMethods(false)
            val psiParameters = psiMethod.parameterList.parameters
            var psiParameter: PsiParameter? = null

            for ((index, psiParam) in psiParameters.withIndex()) {
                var hasAnno = psiParam.hasAnnotation(REQUEST_BODY_ANNO_NAME)
                if (hasAnno) {
                    psiParameter = psiParam
                    break
                }

                for (superPsiMethod in superPsiMethods) {
                    val superPsiParam = superPsiMethod.parameterList.parameters[index]
                    hasAnno = superPsiParam.hasAnnotation(REQUEST_BODY_ANNO_NAME)
                    if (hasAnno) {
                        psiParameter = psiParam
                        break
                    }
                }
            }

            return psiParameter
        }

        fun getUrlControllerMethod(jsonString: JsonStringLiteral): PsiMethod? {
            val project = jsonString.project

            if (!jsonString.isPropertyName) {
                return null
            }

            return getUrlControllerMethod(jsonString, project)
        }

        fun getUrlControllerMethod(psiElement: PsiElement, project: Project): PsiMethod? {
            val messageBody = InjectedLanguageManager.getInstance(project).getInjectionHost(psiElement)
            if (messageBody !is HttpMessageBody) {
                return null
            }

            val httpRequest = PsiTreeUtil.getParentOfType(messageBody, HttpRequest::class.java) ?: return null

            val references = httpRequest.requestTarget?.references ?: return null
            if (references.isEmpty()) {
                return null
            }

            return references[0].resolve() as PsiMethod?
        }

    }

}
