package org.javamaster.httpclient.structure

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.StructureViewModel.ElementInfoProvider
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

class HttpRequestStructureViewFactory : PsiStructureViewFactory {

    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder {
        return object : TreeBasedStructureViewBuilder() {
            override fun createStructureViewModel(editor: Editor?): StructureViewModel {
                val root = HttpRequestStructureViewElement.create(psiFile, "Http Request", null)
                return HttpClientViewModel(psiFile, editor, root)
            }
        }
    }

    private class HttpClientViewModel(psiFile: PsiFile, editor: Editor?, root: StructureViewTreeElement) :
        StructureViewModelBase(psiFile, editor, root), ElementInfoProvider {
        override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean {
            return false
        }

        override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean {
            return false
        }
    }
}
