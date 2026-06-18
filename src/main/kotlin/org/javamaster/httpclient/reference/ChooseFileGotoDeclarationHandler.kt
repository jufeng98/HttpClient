package org.javamaster.httpclient.reference

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.fake.ChooseFileFakePsiElement
import org.javamaster.httpclient.psi.HttpTypes

/**
 * @author yudong
 */
class ChooseFileGotoDeclarationHandler : GotoDeclarationHandler {

    override fun getGotoDeclarationTargets(
        element: PsiElement?,
        offset: Int,
        editor: Editor?,
    ): Array<PsiElement> {
        if (element == null) {
            return arrayOf()
        }

        val elementType = element.elementType

        if (elementType == HttpTypes.INPUT_FILE_SIGN
            || elementType == HttpTypes.OUTPUT_FILE_SIGN
            || elementType == HttpTypes.IMPORT
        ) {
            return arrayOf(ChooseFileFakePsiElement(element, false))
        }

        if (elementType == HttpTypes.DIRECTION_NAME_PART
            && element.text == ParamEnum.IMPORT.param
        ) {
            return arrayOf(ChooseFileFakePsiElement(element, true))
        }

        return arrayOf()
    }

}
