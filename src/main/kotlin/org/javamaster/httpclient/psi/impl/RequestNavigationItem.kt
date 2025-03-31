package org.javamaster.httpclient.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.module.Module
import org.javamaster.httpclient.scan.support.Request
import javax.swing.Icon


class RequestNavigationItem(val request: Request, val module: Module) : ASTWrapperPsiElement(request.psiElement!!.node),
    NavigationItem {
    override fun getPresentation(): ItemPresentation {
        return RequestItemPresentation(request)
    }

    override fun canNavigate(): Boolean {
        return true
    }

    override fun navigate(requestFocus: Boolean) {
        request.psiElement!!.navigate(requestFocus)
    }

    override fun getName(): String {
        return request.path
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
