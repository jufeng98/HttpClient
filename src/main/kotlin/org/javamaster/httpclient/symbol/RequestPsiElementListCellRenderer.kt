package org.javamaster.httpclient.symbol

import com.intellij.ide.util.DefaultModuleRendererFactory
import com.intellij.ide.util.PsiElementListCellRenderer
import com.intellij.psi.PsiElement
import com.intellij.util.TextWithIcon
import org.javamaster.httpclient.psi.impl.RequestNavigationItem

class RequestPsiElementListCellRenderer : PsiElementListCellRenderer<PsiElement>() {
    override fun getElementText(element: PsiElement): String {
        return (element as RequestNavigationItem).request.path
    }

    override fun getContainerText(element: PsiElement, name: String): String {
        return (element as RequestNavigationItem).request.path
    }

    override fun getItemLocation(value: Any?): TextWithIcon? {
        return DefaultModuleRendererFactory.findInstance(value).getModuleTextWithIcon(value)
    }

}
