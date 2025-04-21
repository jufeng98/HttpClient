package org.javamaster.httpclient.curl.support

import com.intellij.openapi.util.text.StringUtil
import com.sun.security.auth.UserPrincipal
import org.apache.http.auth.AuthScope
import org.apache.http.auth.Credentials
import java.security.Principal


class CurlAuthData(private val scope: AuthScope, val authCredentials: Credentials) {

    fun isSchemeEquals(scheme: String): Boolean {
        return StringUtil.equalsIgnoreCase(scope.scheme, scheme)
    }

    companion object {

        val EMPTY_CREDENTIALS: CurlAuthData = CurlAuthData(AuthScope.ANY, object : Credentials {
            override fun getPassword(): String {
                return ""
            }

            override fun getUserPrincipal(): Principal {
                return UserPrincipal("")
            }
        })
    }

}