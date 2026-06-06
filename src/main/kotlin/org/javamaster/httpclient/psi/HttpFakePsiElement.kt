package org.javamaster.httpclient.psi

import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement

/**
 * @author yudong
 */
open class HttpFakePsiElement(val psiElement: PsiElement) : FakePsiElement() {

    override fun canNavigate(): Boolean {
        if (psiElement is Navigatable) {
            return psiElement.canNavigate()
        }

        return false
    }

    override fun canNavigateToSource(): Boolean {
        if (psiElement is Navigatable) {
            return psiElement.canNavigateToSource()
        }

        return false
    }

    override fun getParent(): PsiElement? {
        return psiElement.parent
    }

    override fun getPresentation(): ItemPresentation? {
        if (psiElement is NavigationItem) {
            return psiElement.presentation
        }

        return null
    }
}