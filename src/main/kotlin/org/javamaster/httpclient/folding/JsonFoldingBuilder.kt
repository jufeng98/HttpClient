package org.javamaster.httpclient.folding

import com.intellij.ide.impl.ProjectUtil
import com.intellij.json.JsonElementTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.toArray
import org.apache.commons.lang3.time.DateFormatUtils
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.HttpRequestBlock
import org.javamaster.httpclient.utils.HttpUtils
import java.util.*

/**
 * 可视化时间戳
 *
 * @author yudong
 */
class JsonFoldingBuilder : FoldingBuilder, DumbAware {

    override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
        val activeProject = ProjectUtil.getActiveProject() ?: return FoldingDescriptor.EMPTY_ARRAY
        val injectionHost = InjectedLanguageManager.getInstance(activeProject).getInjectionHost(node.psi)
        if (injectionHost !is HttpMessageBody) {
            return FoldingDescriptor.EMPTY_ARRAY
        }

        val requestBlock = PsiTreeUtil.getParentOfType(injectionHost, HttpRequestBlock::class.java)!!
        val paramMap = HttpUtils.getReqDirectionCommentParamMap(requestBlock)

        if (!paramMap.containsKey(ParamEnum.VISUALIZE_TIMESTAMP.param)) {
            return FoldingDescriptor.EMPTY_ARRAY
        }

        val descriptors = collectDescriptors(node)

        return descriptors.toArray(FoldingDescriptor.EMPTY_ARRAY)
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return DateFormatUtils.format(Date(node.text.toLong()), "yyyy-MM-dd HH:mm:ss")
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return true
    }

    private fun collectDescriptors(node: ASTNode): MutableList<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        val numberNodes = collectNumberNodes(node)

        numberNodes.forEach {
            descriptors.add(FoldingDescriptor(it, it.textRange))
        }

        return descriptors
    }

    private fun collectNumberNodes(node: ASTNode): MutableList<ASTNode> {
        val resList = mutableListOf<ASTNode>()

        var childNode = node.firstChildNode
        while (childNode != null) {
            val elementType = childNode.elementType
            if (elementType == JsonElementTypes.OBJECT
                || elementType == JsonElementTypes.ARRAY
                || elementType == JsonElementTypes.PROPERTY
            ) {
                val list = collectNumberNodes(childNode)

                resList.addAll(list)
            } else if (elementType == JsonElementTypes.NUMBER_LITERAL) {
                if (childNode.textLength == 13) {
                    resList.add(childNode)
                }
            }

            childNode = childNode.treeNext
        }

        return resList
    }

}