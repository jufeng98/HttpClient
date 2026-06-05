package org.javamaster.httpclient.structure.support

import com.intellij.icons.AllIcons
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.navigation.ColoredItemPresentation
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpRequestBlock
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.StructureUtils
import javax.swing.Icon

/**
 * @author yudong
 */
class HttpRequestStructureViewElement(
    element: PsiElement, private val myPresentationText: String, private val myLocation: String?,
    private val myIcon: Icon?, private val myIsValid: Boolean,
) : PsiTreeElementBase<PsiElement?>(element), ColoredItemPresentation {

    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element
        if (element is HttpFile) {
            val children: MutableList<StructureViewTreeElement> = mutableListOf()

            val globalImports = element.getGlobalImports()
            for (globalImport in globalImports) {
                var location = HttpUtils.getFilePathText(globalImport.filePath)

                val viewElement = StructureUtils.create(
                    globalImport, "import", location, AllIcons.ToolbarDecorator.Import, true
                )

                children.add(viewElement)
            }

            val directionComments = element.getDirectionComments()
            for (directionComment in directionComments) {
                val text = directionComment.directionName?.text ?: ""
                var location = directionComment.directionValue?.text

                val viewElement = StructureUtils.create(
                    directionComment, text, location, AllIcons.ToolbarDecorator.Import, true
                )

                children.add(viewElement)
            }

            val globalHandler = element.getGlobalHandler()
            if (globalHandler != null) {
                val viewElement = StructureUtils.create(
                    globalHandler, NlsBundle.nls("global.handler"), AllIcons.Actions.Play_first
                )

                children.add(viewElement)
            }

            val fileVariables = element.getFileVariables()
            fileVariables.forEach {
                val viewElement = StructureUtils.create(
                    it, NlsBundle.nls("file.variable"), it.text, AllIcons.General.InlineVariables, true
                )

                children.add(viewElement)
            }

            val blocks = element.getRequestBlocks()
            for (block in blocks) {
                val viewElement = StructureUtils.create(block)

                children.add(viewElement)
            }

            return children
        } else if (element is HttpRequestBlock) {
            return StructureUtils.getRequestBlockChildren(element)
        }

        return emptyList()
    }

    override fun getLocationString(): String? {
        return myLocation
    }

    override fun isSearchInLocationString(): Boolean {
        return true
    }

    override fun getTextAttributesKey(): TextAttributesKey? {
        return if (myIsValid) null else CodeInsightColors.ERRORS_ATTRIBUTES
    }

    override fun getPresentableText(): String {
        return myPresentationText
    }

    override fun getIcon(open: Boolean): Icon? {
        return myIcon ?: super.getIcon(open)
    }

}