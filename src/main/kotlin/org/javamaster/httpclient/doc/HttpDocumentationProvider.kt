package org.javamaster.httpclient.doc

import com.intellij.json.JsonElementTypes
import com.intellij.json.psi.JsonLiteral
import com.intellij.json.psi.JsonProperty
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.FakePsiElement
import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.env.EnvFileService.Companion.getJsonLiteralValue
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.*
import org.javamaster.httpclient.reference.support.TextVariableNamePsiReference
import org.javamaster.httpclient.resolve.VariableResolver.Companion.ENV_PREFIX
import org.javamaster.httpclient.resolve.VariableResolver.Companion.PROPERTY_PREFIX
import org.jetbrains.annotations.Nls
import javax.swing.Icon

/**
 * @author yudong
 */
class HttpDocumentationProvider : DocumentationProvider {

    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): @Nls String? {
        originalElement ?: return null

        val psiFile = InjectedLanguageManager.getInstance(originalElement.project).getTopLevelFile(originalElement)
        if (psiFile !is HttpFile) {
            return null
        }

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

        if (element is HttpGlobalVariableName) {
            val name = element.name
            val parent = element.parent as HttpGlobalVariable
            val globalVariableValue = parent.globalVariableValue

            return getDocumentation(
                name,
                "The value is: " + (globalVariableValue?.value ?: globalVariableValue?.variable?.text)
            )
        }

        if (element is HttpDirectionName) {
            val name = element.text
            val paramEnum = ParamEnum.getEnum(name) ?: return null

            return getDocumentation(name, paramEnum.desc)
        }

        if (element is JsonProperty) {
            val match = originalElement.parent is HttpVariableReference || (element.value is JsonLiteral
                    && HttpPsiUtils.getPrevSiblingByType(originalElement.parent, JsonElementTypes.COLON, false) != null)
            if (match) {
                val name = element.name
                val value = getJsonLiteralValue(element.value as JsonLiteral)

                return getDocumentation(name, "The value is: $value")
            }
        }

        val psiElement = originalElement.parent?.parent
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
        contextElement ?: return null

        val psiFile = InjectedLanguageManager.getInstance(file.project).getTopLevelFile(file)
        if (psiFile !is HttpFile) {
            return null
        }

        val parent = contextElement.parent
        if (parent is HttpDirectionName) {
            return parent
        }

        val element = parent?.parent
        if (element is HttpVariableName) {
            return element
        }

        val psiReferences = mutableListOf<PsiReference>()
        psiReferences.addAll(contextElement.references)
        psiReferences.addAll(parent?.references ?: emptyArray())

        for (psiReference in psiReferences) {
            if (psiReference !is TextVariableNamePsiReference) continue

            val textRange = psiReference.textRange
            if (targetOffset < textRange.startOffset || targetOffset > textRange.endOffset) continue

            return MyFakePsiElement(contextElement, psiReference.variable)
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