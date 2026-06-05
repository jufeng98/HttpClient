package org.javamaster.httpclient.structure

import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.psi.PsiFile
import org.javamaster.httpclient.structure.support.HttpTreeBasedStructureViewBuilder

/**
 * @author yudong
 */
class HttpRequestStructureViewFactory : PsiStructureViewFactory {

    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder {
        return HttpTreeBasedStructureViewBuilder(psiFile)
    }

}
