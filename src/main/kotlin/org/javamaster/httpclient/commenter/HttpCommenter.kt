package org.javamaster.httpclient.commenter

import com.intellij.lang.CodeDocumentationAwareCommenterEx
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import org.javamaster.httpclient.psi.HttpTypes

/**
 * @author yudong
 */
class HttpCommenter : CodeDocumentationAwareCommenterEx {

    override fun getLineCommentPrefix(): String {
        return SLASH_COMMENT_PREFIX
    }

    override fun getBlockCommentPrefix(): String {
        return BLOCK_COMMENT_START
    }

    override fun getBlockCommentSuffix(): String {
        return BLOCK_COMMENT_END
    }

    override fun getCommentedBlockCommentPrefix(): String? {
        return null
    }

    override fun getCommentedBlockCommentSuffix(): String? {
        return null
    }

    override fun getLineCommentTokenType(): IElementType {
        return HttpTypes.LINE_COMMENT
    }

    override fun getBlockCommentTokenType(): IElementType {
        return HttpTypes.BLOCK_COMMENT
    }

    override fun getDocumentationCommentTokenType(): IElementType? {
        return null
    }

    override fun getDocumentationCommentPrefix(): String? {
        return null
    }

    override fun getDocumentationCommentLinePrefix(): String? {
        return null
    }

    override fun getDocumentationCommentSuffix(): String? {
        return null
    }

    override fun isDocumentationComment(element: PsiComment?): Boolean {
        return false
    }

    override fun isDocumentationCommentText(element: PsiElement?): Boolean {
        return false
    }

    companion object {
        private const val SLASH_COMMENT_PREFIX = "//"
        private const val BLOCK_COMMENT_START = "/*"
        private const val BLOCK_COMMENT_END = "*/"
    }
}