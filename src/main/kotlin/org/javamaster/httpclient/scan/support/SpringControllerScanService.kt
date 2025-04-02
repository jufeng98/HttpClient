package org.javamaster.httpclient.scan.support

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifierList
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex
import com.intellij.psi.impl.search.JavaSourceFilterScope
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
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
import java.util.function.Consumer

/**
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class SpringControllerScanService {

    fun findRequests(project: Project, searchScope: GlobalSearchScope): List<Request> {
        val requests = mutableListOf<Request>()

        fetchRequests(project, searchScope) {
            requests.add(it)
        }

        return requests
    }

    fun fetchRequests(project: Project, scope: GlobalSearchScope, consumer: Consumer<Request>) {
        val annotationIndex = JavaAnnotationIndex.getInstance()

        val annotations = StubIndex.getElements(
            annotationIndex.key, Control.Controller.simpleName, project, JavaSourceFilterScope(scope),
            PsiAnnotation::class.java
        )

        iterateControllers(annotations, consumer)

        val annotationsRest = StubIndex.getElements(
            annotationIndex.key, Control.RestController.simpleName, project, JavaSourceFilterScope(scope),
            PsiAnnotation::class.java
        )

        iterateControllers(annotationsRest, consumer)
    }

    private fun iterateControllers(controllerAnnoList: Collection<PsiAnnotation>, consumer: Consumer<Request>) {
        controllerAnnoList
            .forEach { controllerAnno ->
                val psiModifierList = controllerAnno.parent as PsiModifierList
                val controllerClass = psiModifierList.parent as PsiClass? ?: return@forEach

                val psiAnnotation = getClassAnnotation(
                    controllerClass,
                    SpringHttpMethod.REQUEST_MAPPING.shortName,
                    SpringHttpMethod.REQUEST_MAPPING.qualifiedName
                )

                val childrenRequests: MutableList<Request> = mutableListOf()
                var parentRequests: List<Request> = mutableListOf()

                if (psiAnnotation != null) {
                    parentRequests = getRequests(psiAnnotation, null)

                }

                val requests = controllerClass.allMethods
                    .map { getRequests(it) }
                    .flatten()

                childrenRequests.addAll(requests)

                if (parentRequests.isEmpty()) {
                    childrenRequests.forEach { consumer.accept(it) }
                } else {
                    parentRequests.forEach { parentRequest ->
                        childrenRequests.forEach {
                            val request = it.copyWithParent(parentRequest)

                            consumer.accept(request)
                        }
                    }
                }
            }
    }

    private fun getRequests(method: PsiMethod): List<Request> {
        val methodAnnotations = collectMethodAnnotations(method)

        return methodAnnotations
            .map { getRequests(it, method) }
            .flatten()
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
                    System.err.println(String.format("Scan api: %s,Class: %s", value, value?.javaClass))
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
