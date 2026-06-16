package org.javamaster.httpclient.reference.provider

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.psi.HttpLineComment
import org.javamaster.httpclient.reference.support.HttpLineCommentPsiReference

/**
 * @author yudong
 */
class HttpLineCommentPsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val lineComment = element as HttpLineComment
        val comment = lineComment.text

        val key1 = "// " + nls("save.to.file", "")
        if (comment.startsWith(key1)) {
            return createReference(key1, lineComment, comment)
        }

        val key2 = "// " + nls("cookie.saved", "")
        if (comment.startsWith(key2)) {
            return createReference(key2, lineComment, comment)
        }

        val key3 = "// " + nls("cookie.saved.failed.ignored", "")
        if (comment.startsWith(key3)) {
            return createReference(key3, lineComment, comment)
        }

        val key4 = "// " + nls("cookie.saved.failed.excluded", "")
        if (comment.startsWith(key4)) {
            return createReference(key4, lineComment, comment)
        }

        return emptyArray()
    }

    private fun createReference(key: String, lineComment: HttpLineComment, comment: String): Array<PsiReference> {
        val keyLength = key.length
        val range = TextRange(keyLength, comment.length)
        val filePath = comment.substring(keyLength).trimEnd()
        return arrayOf(HttpLineCommentPsiReference(lineComment, range, filePath))
    }

}
