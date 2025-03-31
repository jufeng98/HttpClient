package org.javamaster.httpclient.scan.support

import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifierList
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex
import org.javamaster.httpclient.enums.Control
import org.javamaster.httpclient.enums.HttpMethod
import org.javamaster.httpclient.enums.HttpMethod.Companion.parse
import org.javamaster.httpclient.enums.SpringHttpMethod
import org.javamaster.httpclient.enums.SpringHttpMethod.Companion.getByQualifiedName
import org.javamaster.httpclient.enums.SpringHttpMethod.Companion.getByShortName
import org.javamaster.httpclient.utils.AnnoUtils
import org.javamaster.httpclient.utils.AnnoUtils.collectMethodAnnotations
import org.javamaster.httpclient.utils.AnnoUtils.findAnnotationValue
import org.javamaster.httpclient.utils.AnnoUtils.getClassAnnotation
import org.javamaster.httpclient.utils.AnnoUtils.getQualifiedAnnotation

/**
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class SpringControllerScanService {

    fun getSpringMvcRequest(project: Project, module: Module, progressIndicator: ProgressIndicator?): List<Request> {
        val controllerClasses = getControllerClasses(project, module, progressIndicator)
        if (controllerClasses.isEmpty()) {
            return emptyList()
        }

        return controllerClasses
            .map {
                progressIndicator?.checkCanceled()

                getRequests(it)
            }
            .flatten()
    }

    private fun getRequests(controllerClass: PsiClass): List<Request> {
        val requests: MutableList<Request> = mutableListOf()
        var parentRequests: List<Request> = mutableListOf()
        val childrenRequests: MutableList<Request> = mutableListOf()

        val psiAnnotation = getClassAnnotation(
            controllerClass,
            SpringHttpMethod.REQUEST_MAPPING.shortName,
            SpringHttpMethod.REQUEST_MAPPING.qualifiedName
        )

        if (psiAnnotation != null) {
            parentRequests = getRequests(psiAnnotation, null)
        }

        val requestList = controllerClass.allMethods
            .map { getRequests(it) }
            .flatten()

        childrenRequests.addAll(requestList)

        if (parentRequests.isEmpty()) {
            requests.addAll(childrenRequests)
        } else {
            parentRequests.forEach { parentRequest ->
                childrenRequests.forEach {
                    val request = it.copyWithParent(parentRequest)
                    requests.add(request)
                }
            }
        }

        return requests
    }

    private fun getControllerClasses(
        project: Project,
        module: Module,
        progressIndicator: ProgressIndicator?,
    ): List<PsiClass> {
        progressIndicator?.checkCanceled()

        val annoList: MutableCollection<PsiAnnotation> = mutableListOf()
        val moduleScope = module.moduleWithLibrariesScope

        val annotations = JavaAnnotationIndex.getInstance().getAnnotations(
            Control.Controller.simpleName,
            project,
            moduleScope
        )

        annoList.addAll(annotations)

        val annotationsRest = JavaAnnotationIndex.getInstance().getAnnotations(
            Control.RestController.simpleName,
            project,
            moduleScope
        )

        annoList.addAll(annotationsRest)

        return annoList
            .mapNotNull {
                val psiModifierList = it.parent as PsiModifierList
                psiModifierList.parent as PsiClass?
            }
    }

    private fun getRequests(annotation: PsiAnnotation, psiMethod: PsiMethod?): List<Request> {
        var httpMethod = getByQualifiedName(annotation.qualifiedName)

        if (httpMethod == null) {
            httpMethod = getByShortName(annotation.nameReferenceElement?.text)
        }

        val methods: MutableSet<HttpMethod> = mutableSetOf()
        val paths: MutableList<String> = mutableListOf()
        var refAnnotation: CustomRefAnnotation? = null

        if (httpMethod == null) {
            refAnnotation = findCustomAnnotation(annotation)
            if (refAnnotation == null) {
                return emptyList()
            }

            methods.addAll(refAnnotation.methods)
        } else {
            methods.add(httpMethod.method)
        }

        var hasImplicitPath = true
        val attributes = annotation.attributes
        for (attribute in attributes) {
            val name = attribute.attributeName

            if (methods.contains(HttpMethod.REQUEST) && "method" == name) {
                val value = AnnoUtils.getAttributeValue(attribute.attributeValue)
                if (value is String) {
                    methods.add(parse(value))
                } else if (value is List<*>) {
                    for (item in value) {
                        if (item != null) {
                            var tmp = item.toString()
                            tmp = tmp.substring(tmp.lastIndexOf(".") + 1)
                            methods.add(parse(tmp))
                        }
                    }
                }
            }

            var flag = false
            for (path in arrayOf("value", "path")) {
                if (path == name) {
                    flag = true
                    break
                }
            }

            if (!flag) {
                continue
            }

            when (val value = AnnoUtils.getAttributeValue(attribute.attributeValue)) {
                is String -> {
                    paths.add(formatPath(value))
                }

                is List<*> -> {
                    value.forEach { paths.add(formatPath(it)) }
                }

                else -> {
                    throw RuntimeException(String.format("Scan api: %s,Class: %s", value, value?.javaClass))
                }
            }

            hasImplicitPath = false
        }

        if (hasImplicitPath && psiMethod != null) {
            if (refAnnotation != null) {
                paths.addAll(refAnnotation.paths)
            } else {
                paths.add("/")
            }
        }

        return paths
            .map {
                methods
                    .filter { it != HttpMethod.REQUEST || methods.size <= 1 }
                    .map { method -> Request(method, it, psiMethod, null) }
            }
            .flatten()
    }

    private fun getRequests(method: PsiMethod): List<Request> {
        val methodAnnotations = collectMethodAnnotations(method)

        return methodAnnotations
            .map { getRequests(it, method) }
            .flatten()
    }

    private fun findCustomAnnotation(psiAnnotation: PsiAnnotation): CustomRefAnnotation? {
        val qualifiedAnnotation = getQualifiedAnnotation(
            psiAnnotation,
            SpringHttpMethod.REQUEST_MAPPING.qualifiedName
        )

        if (qualifiedAnnotation == null) {
            return null
        }

        val otherAnnotation = CustomRefAnnotation()

        for (attribute in qualifiedAnnotation.attributes) {
            val methodValues = findAnnotationValue(attribute, "method")

            if (methodValues != null) {
                val methods = if (methodValues is List<*>) methodValues else listOf(methodValues)
                if (methods.isEmpty()) {
                    continue
                }

                for (method in methods) {
                    if (method == null) {
                        continue
                    }

                    val parseMethods = parse(method)
                    otherAnnotation.addMethods(parseMethods)
                }
                continue
            }

            val pathValues = findAnnotationValue(attribute, "path", "value")

            if (pathValues != null) {
                val paths = if (pathValues is List<*>) pathValues else listOf(pathValues)
                for (path in paths) {
                    if (path == null) {
                        continue
                    }

                    otherAnnotation.addPath(path as String)
                }
            }
        }

        return otherAnnotation
    }


    private fun formatPath(path: Any?): String {
        val slash = "/"
        if (path == null) {
            return slash
        }

        val currPath = if (path is String) {
            path
        } else {
            path.toString()
        }

        if (currPath.startsWith(slash)) {
            return currPath
        }

        return slash + currPath
    }

    companion object {
        fun getService(project: Project): SpringControllerScanService {
            return project.getService(SpringControllerScanService::class.java)
        }
    }
}
