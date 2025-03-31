package org.javamaster.httpclient.psi.impl

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement
import org.javamaster.httpclient.scan.support.Request
import javax.swing.Icon


class RequestNavigationItem(val request: Request, val module: Module) :
    FakePsiElement() {
    val psiMethod = request.psiElement!!

    override fun getPresentation(): ItemPresentation {
        return RequestItemPresentation(request)
    }

    override fun canNavigate(): Boolean {
        return true
    }

    override fun navigate(requestFocus: Boolean) {
        psiMethod.navigate(requestFocus)
    }

    override fun getName(): String {
        return request.path
    }

    override fun getContext(): PsiElement? {
        return psiMethod.context
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestNavigationItem

        if (request != other.request) return false
        if (module != other.module) return false

        return true
    }

    override fun hashCode(): Int {
        var result = request.hashCode()
        result = 31 * result + module.hashCode()
        return result
    }

    override fun getParent(): PsiElement {
        return psiMethod
    }

    override fun getNavigationElement(): PsiElement {
        return psiMethod
    }

    class RequestItemPresentation(val request: Request) : ItemPresentation {
        override fun getPresentableText(): String {
            return request.path
        }

        override fun getIcon(unused: Boolean): Icon {
            return request.method.icon
        }

    }

}
