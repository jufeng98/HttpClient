package org.javamaster.httpclient.structure.support

import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import org.javamaster.httpclient.utils.StructureUtils

/**
 * @author yudong
 */
class HttpTreeBasedStructureViewBuilder(private val psiFile: PsiFile) : TreeBasedStructureViewBuilder() {

    override fun createStructureViewModel(editor: Editor?): StructureViewModel {
        val root = StructureUtils.create(psiFile, "Http Request", null)

        return HttpStructureViewModel(psiFile, editor, root)
    }

    override fun isRootNodeShown(): Boolean {
        return false
    }

}
