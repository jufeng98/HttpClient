package org.javamaster.httpclient.doc

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.FakePsiElement
import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpVariable
import org.javamaster.httpclient.psi.HttpVariableName
import org.javamaster.httpclient.reference.support.TextVariableNamePsiReference
import org.javamaster.httpclient.resolve.VariableResolver.Companion.ENV_PREFIX
import org.javamaster.httpclient.resolve.VariableResolver.Companion.PROPERTY_PREFIX
import org.jetbrains.annotations.Nls
import javax.swing.Icon


class HttpDocumentationProvider : DocumentationProvider {

    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): @Nls String? {
        if (element is MyFakePsiElement) {
            val variable = element.variable
            val variableName = variable.variableName ?: return null

            val name = variableName.name

            return getHttpDoc(name)
        }

        if (element is HttpVariableName) {
            val name = element.name

            return getHttpDoc(name)
        }

        val psiElement = originalElement?.parent?.parent
        if (psiElement is HttpVariableName) {
            val name = psiElement.name
            val variableEnum = InnerVariableEnum.getEnum(name)

            if (variableEnum != null && element is PsiDirectory) {
                return getDocumentation(name, variableEnum.typeText() + ", the value is: ${element.virtualFile.path}")
            }
        }

        return null
    }

    override fun getCustomDocumentationElement(
        editor: Editor, file: PsiFile,
        contextElement: PsiElement?,
        targetOffset: Int,
    ): PsiElement? {
        val psiFile = InjectedLanguageManager.getInstance(file.project).getTopLevelFile(file)
        if (psiFile !is HttpFile) {
            return null
        }

        val element = contextElement?.parent?.parent
        if (element is HttpVariableName) {
            return element
        }

        val psiReferences = contextElement?.parent?.references ?: return null
        for (psiReference in psiReferences) {
            if (psiReference is TextVariableNamePsiReference) {
                val textRange = psiReference.textRange
                if (targetOffset >= textRange.startOffset && targetOffset <= textRange.endOffset) {
                    return MyFakePsiElement(contextElement, psiReference.variable)
                }
            }
        }

        return contextElement
    }

    private fun getHttpDoc(name: String): String? {
        val variableEnum = InnerVariableEnum.getEnum(name)
        if (variableEnum != null) {
            return getDocumentation(name, variableEnum.typeText())
        }

        if (name.startsWith(ENV_PREFIX)) {
            val key = name.substring(ENV_PREFIX.length + 1)
            return getDocumentation(name, "Means System.getenv(\"${key}\"), the value is: ${System.getenv(key)}")
        }

        if (name.startsWith(PROPERTY_PREFIX)) {
            val key = name.substring(PROPERTY_PREFIX.length + 1)
            return getDocumentation(
                name,
                "Means System.getProperty(\"${key}\"), the value is: ${System.getProperty(key)}"
            )
        }

        return null
    }

    private fun getDocumentation(identifier: String, description: String): String {
        return "<div class='definition'><pre>$identifier</pre></div>$description"
    }

    private class MyFakePsiElement(val contextElement: PsiElement, val variable: HttpVariable) : FakePsiElement() {
        override fun getParent(): PsiElement {
            return contextElement
        }

        override fun getPresentation(): ItemPresentation {
            return MyItemPresentation
        }

        object MyItemPresentation : ItemPresentation {
            override fun getPresentableText(): String {
                return ""
            }

            override fun getIcon(unused: Boolean): Icon? {
                return null
            }
        }
    }
}