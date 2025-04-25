package org.javamaster.httpclient.gutter.support

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.Function
import java.util.function.Supplier
import javax.swing.Icon

/**
 * @author yudong
 */
class HttpLineMarkerInfo(
    element: PsiElement,
    range: TextRange,
    icon: Icon,
    tooltipProvider: Function<PsiElement, String>,
    navHandler: GutterIconNavigationHandler<PsiElement>,
    alignment: GutterIconRenderer.Alignment,
    accessibleNameProvider: Supplier<String>,
) :
    LineMarkerInfo<PsiElement>(element, range, icon, tooltipProvider, navHandler, alignment, accessibleNameProvider)
