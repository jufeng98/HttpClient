package org.javamaster.httpclient.structure.support

import com.intellij.ide.structureView.StructureViewModel.ElementInfoProvider
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

/**
 * @author yudong
 */
class HttpStructureViewModel(psiFile: PsiFile, editor: Editor?, root: StructureViewTreeElement) :
    StructureViewModelBase(psiFile, editor, root), ElementInfoProvider {

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean {
        return false
    }

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean {
        return false
    }
}
