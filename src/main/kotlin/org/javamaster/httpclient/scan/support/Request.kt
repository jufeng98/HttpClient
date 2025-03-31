package org.javamaster.httpclient.scan.support

import com.intellij.psi.PsiMethod
import org.javamaster.httpclient.enums.HttpMethod

class Request(
    tmpMethod: HttpMethod,
    tmpPath: String,
    val psiElement: PsiMethod?,
    parent: Request?,
) {
    val method: HttpMethod
    val path: String

    init {
        if (parent == null) {
            var path = tmpPath.trim()
            if (!path.startsWith("/")) {
                path = "/$path"
            }

            path = path.replace("//", "/")

            this.method = tmpMethod
            this.path = path
        } else {
            if (tmpMethod == HttpMethod.REQUEST) {
                this.method = parent.method
            } else {
                this.method = tmpMethod
            }

            var parentPath = parent.path
            if (parentPath.endsWith("/")) {
                parentPath = parentPath.substring(0, parentPath.length - 1)
            }

            this.path = parentPath + tmpPath
        }
    }

    fun copyWithParent(parent: Request): Request {
        return Request(this.method, this.path, this.psiElement, parent)
    }

    override fun toString(): String {
        return this.path + "-" + method.name
    }

}
