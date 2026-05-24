package org.javamaster.httpclient.consts

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Key
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.enums.SimpleTypeEnum

/**
 * @author yudong
 */
class HttpConsts {

    companion object {
        const val REQUEST_BODY_ANNO_NAME = "org.springframework.web.bind.annotation.RequestBody"
        const val API_OPERATION_ANNO_NAME = "io.swagger.annotations.ApiOperation"
        const val API_MODEL_PROPERTY_ANNO_NAME = "io.swagger.annotations.ApiModelProperty"

        const val READ_TIMEOUT = 3600L
        const val CONNECT_TIMEOUT = 30L
        const val TIMEOUT = 10_000

        const val RES_SIZE_LIMIT = (1.5 * 1024 * 1024).toInt()

        const val HTTP_TYPE_ID = "intellijHttpClient"
        const val WEB_BOUNDARY = "boundary"
        const val VARIABLE_SIGN_END = "}}"
        const val SUCCESS = 0
        const val FAILED = 1

        val gutterIconLoadingKey: Key<Runnable?> = Key.create("GUTTER_ICON_LOADING_KEY")
        val requestFinishedKey: Key<Int> = Key.create("REQUEST_FINISHED_KEY")
        val httpDashboardContentTypeKey = Key.create<ContentType>("org.javamaster.dashboard.httpDashboardContentType")
        val httpDashboardFoldHeaderKey = Key.create<Boolean>("org.javamaster.dashboard.httpDashboardFoldHeader")
        val httpDashboardToolbarKey = Key.create<Boolean>("org.javamaster.dashboard.httpDashboardToolbar")
        val httpDashboardResTypeKey = Key.create<SimpleTypeEnum?>("org.javamaster.dashboard.httpDashboardResType")
        val httpDashboardReqEditorKey = Key.create<Editor>("org.javamaster.dashboard.httpDashboardReqEditor")
        val httpDashboardResEditorKey = Key.create<Editor>("org.javamaster.dashboard.httpDashboardResEditor")

        const val COOKIE_FILE_NAME = "http-client.cookies"
        const val JS_DATE_PATTERN = "EEE, dd-MMM-yyyy HH:mm:ss 'GMT'"
        const val JAVA_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss"
    }

}