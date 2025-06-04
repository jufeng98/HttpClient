package org.javamaster.httpclient.formatter

import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.formatting.FormattingModelProvider

/**
 * @author yudong
 */
class HttpFormattingModelBuilder : FormattingModelBuilder {

    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val containingFile = formattingContext.containingFile

        val fileNode = containingFile.node

        val codeStyleSettings = formattingContext.codeStyleSettings

        val fileBlock = HttpRequestFileBlock(fileNode, codeStyleSettings)

        return FormattingModelProvider.createFormattingModelForPsiFile(containingFile, fileBlock, codeStyleSettings)
    }

}
