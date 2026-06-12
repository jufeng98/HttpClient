// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi.impl

import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import com.intellij.psi.tree.IElementType
import org.javamaster.httpclient.psi.HttpLineComment

/**
 * @author yudong
 */
class HttpLineCommentImpl(type: IElementType, text: CharSequence) : PsiCommentImpl(type, text), HttpLineComment {

    override fun getReferences(): Array<out PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this)
    }

}
