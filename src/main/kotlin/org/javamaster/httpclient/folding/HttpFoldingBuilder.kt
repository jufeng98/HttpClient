package org.javamaster.httpclient.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.tree.TokenSet
import com.intellij.util.containers.toArray
import org.javamaster.httpclient.psi.HttpMultipartField
import org.javamaster.httpclient.psi.HttpTypes
import org.javamaster.httpclient.psi.impl.HttpPsiImplUtil.getHeaderFieldOption
import org.javamaster.httpclient.psi.impl.HttpPsiImplUtil.getMultipartFieldDescription


class HttpFoldingBuilder : FoldingBuilder, DumbAware {
    override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
        val descriptors: MutableList<FoldingDescriptor> = ArrayList()

        collectDescriptors(node, descriptors)

        return descriptors.toArray(FoldingDescriptor.EMPTY_ARRAY)
    }

    override fun getPlaceholderText(node: ASTNode): String {
        val type = node.elementType
        if (type === HttpTypes.REQUEST) {
            val httpMethodNode = node.findChildByType(HttpTypes.METHOD)
            val httpRequestTargetNode = node.findChildByType(HttpTypes.REQUEST_TARGET)
            if (httpMethodNode != null) {
                val methodType = httpMethodNode.text
                return methodType + (if (httpRequestTargetNode != null) " " + httpRequestTargetNode.text else "")
            }
        } else if (type === HttpTypes.MULTIPART_FIELD) {
            val messagesGroupNode = node.findChildByType(HttpTypes.REQUEST_MESSAGES_GROUP)
            val contentDispositionName = getContentDispositionName(node)
            if (contentDispositionName != null) {
                if (messagesGroupNode != null) {
                    return contentDispositionName + ": " + messagesGroupNode.text
                }

                return contentDispositionName
            }

            if (messagesGroupNode != null) {
                return messagesGroupNode.text
            }

            if (node.firstChildNode != null) {
                return node.firstChildNode.text
            }
        } else if (type === HttpTypes.RESPONSE_HANDLER) {
            return "{% ... %}"
        }

        return "..."
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }

    override fun isCollapsedByDefault(foldingDescriptor: FoldingDescriptor): Boolean {
        return foldingDescriptor is DifferenceFilesFoldingDescriptor
    }

    private class DifferenceFilesFoldingDescriptor(parent: ASTNode, differenceFiles: Array<ASTNode>, limit: Int) :
        FoldingDescriptor(
            parent,
            getRange(differenceFiles, limit),
            null as FoldingGroup?,
            "place holder",
            true,
            setOf()
        ) {
        init {
            this.isGutterMarkEnabledForSingleLine = true
            this.setCanBeRemovedWhenCollapsed(true)
        }

        companion object {
            private fun getRange(differenceFiles: Array<ASTNode>, limit: Int): TextRange {
                assert(differenceFiles.size > limit)

                val firstToHide = differenceFiles[limit]
                val lastToHide = differenceFiles[differenceFiles.size - 1]

                return TextRange(firstToHide.startOffset, lastToHide.startOffset + lastToHide.textLength)
            }
        }
    }

    private fun collectDescriptors(node: ASTNode, descriptors: MutableList<FoldingDescriptor>) {
        val requestBlocks = node.getChildren(TokenSet.create(HttpTypes.REQUEST_BLOCK))

        for (requestBlockNode in requestBlocks) {
            val requestNode = requestBlockNode.findChildByType(HttpTypes.REQUEST) ?: continue

            collectMultipartRequests(requestNode, descriptors)

            collectScriptPart(requestNode, descriptors)

            if (requestNode.findChildByType(HttpTypes.METHOD) != null) {
                descriptors.add(FoldingDescriptor(requestNode, requestNode.textRange))
            }
        }
    }

    private fun collectMultipartRequests(node: ASTNode, descriptors: MutableList<FoldingDescriptor>) {
        val multipartMessage: ASTNode?
        val multipart = node.findChildByType(HttpTypes.MULTIPART_MESSAGE)
            .also { multipartMessage = it }

        if (multipart == null) return

        val multipartFields = multipartMessage!!.getChildren(TokenSet.create(HttpTypes.MULTIPART_FIELD))

        for (multipartFieldNode in multipartFields) {
            val prevElement = skipCommentsAndWhitespaces(multipartFieldNode)
            val startOffset =
                if (prevElement != null && prevElement.elementType === HttpTypes.MESSAGE_BOUNDARY) {
                    prevElement.textRange.startOffset
                } else {
                    multipartFieldNode.textRange.startOffset
                }

            descriptors.add(
                FoldingDescriptor(
                    multipartFieldNode,
                    TextRange(startOffset, multipartFieldNode.textRange.endOffset)
                )
            )
        }
    }

    private fun collectScriptPart(node: ASTNode, descriptors: MutableList<FoldingDescriptor>) {
        val responseHandlerNode = node.findChildByType(HttpTypes.RESPONSE_HANDLER) ?: return

        descriptors.add(FoldingDescriptor(responseHandlerNode, responseHandlerNode.textRange))
    }

    private fun skipCommentsAndWhitespaces(node: ASTNode): ASTNode? {
        var curNode: ASTNode?
        curNode = node.treePrev
        while (curNode != null && (curNode.psi is PsiWhiteSpace)) {
            curNode = curNode.treePrev
        }

        return curNode
    }

    private fun getContentDispositionName(node: ASTNode): String? {
        val psiElement = node.psi
        if (psiElement !is HttpMultipartField) return null

        val headerFieldValue = getMultipartFieldDescription((node.psi as HttpMultipartField)) ?: return null

        return getHeaderFieldOption(headerFieldValue, "name")
    }

}