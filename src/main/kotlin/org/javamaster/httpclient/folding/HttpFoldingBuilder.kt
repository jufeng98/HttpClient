package org.javamaster.httpclient.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.tree.TokenSet
import com.intellij.util.containers.toArray
import org.javamaster.httpclient.psi.HttpHeader
import org.javamaster.httpclient.psi.HttpMultipartField
import org.javamaster.httpclient.psi.HttpTypes
import org.javamaster.httpclient.psi.impl.HttpPsiImplUtil.getHeaderFieldOption
import org.javamaster.httpclient.psi.impl.HttpPsiImplUtil.getMultipartFieldDescription


class HttpFoldingBuilder : FoldingBuilder, DumbAware {
    override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
        val descriptors = collectDescriptors(node)

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
        } else if (type == HttpTypes.GLOBAL_HANDLER) {
            return "{% ... %}"
        } else if (type == HttpTypes.HEADER) {
            val contentTypeField = (node.psi as HttpHeader).contentTypeField
            if (contentTypeField != null) {
                return "(Headers)...${contentTypeField.text}..."
            }
            return "(Headers)..."
        }

        return "..."
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        val type = node.elementType
        return type == HttpTypes.HEADER
    }

    private fun collectDescriptors(node: ASTNode): MutableList<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        val requestBlockNodes = node.getChildren(TokenSet.create(HttpTypes.REQUEST_BLOCK))

        val globalHandlerNode = node.findChildByType(HttpTypes.GLOBAL_HANDLER)
        if (globalHandlerNode != null) {
            descriptors.add(FoldingDescriptor(globalHandlerNode, globalHandlerNode.textRange))
        }

        for (requestBlockNode in requestBlockNodes) {
            val requestNode = requestBlockNode.findChildByType(HttpTypes.REQUEST) ?: continue

            val list = collectMultipartRequests(requestNode)
            descriptors.addAll(list)

            val parts = collectScriptPart(requestNode, requestBlockNode)
            descriptors.addAll(parts)

            if (requestNode.findChildByType(HttpTypes.METHOD) != null) {
                descriptors.add(FoldingDescriptor(requestNode, requestNode.textRange))
            }

            val header = requestNode.findChildByType(HttpTypes.HEADER)
            if (header != null) {
                descriptors.add(FoldingDescriptor(header, header.textRange))
            }
        }

        return descriptors
    }

    private fun collectMultipartRequests(node: ASTNode): MutableList<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        val bodyNode = node.findChildByType(HttpTypes.BODY) ?: return descriptors

        val multipartMessage: ASTNode?
        val multipart = bodyNode.findChildByType(HttpTypes.MULTIPART_MESSAGE)
            .also { multipartMessage = it }

        if (multipart == null) return descriptors

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

        return descriptors
    }

    private fun collectScriptPart(
        requestNode: ASTNode,
        requestBlockNode: ASTNode,
    ): MutableList<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        val responseHandlerNode = requestNode.findChildByType(HttpTypes.RESPONSE_HANDLER)
        if (responseHandlerNode != null) {
            descriptors.add(FoldingDescriptor(responseHandlerNode, responseHandlerNode.textRange))
        }

        val preHandlerNode = requestBlockNode.findChildByType(HttpTypes.PRE_REQUEST_HANDLER)
        if (preHandlerNode != null) {
            descriptors.add(FoldingDescriptor(preHandlerNode, preHandlerNode.textRange))
        }

        return descriptors
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

        val headerFieldValue = getMultipartFieldDescription(node.psi as HttpMultipartField) ?: return null

        return getHeaderFieldOption(headerFieldValue, "name")
    }

}