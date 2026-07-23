package org.javamaster.httpclient.utils

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe

object SensitiveDataUtil {

    fun get(key: String): String? {
        val attributes = createCredentialAttributes(key)
        val passwordSafe = PasswordSafe.instance

        val credentials = passwordSafe.get(attributes)
        return credentials?.getPasswordAsString()
    }

    fun save(key: String, value: String?) {
        val attributes = createCredentialAttributes(key)
        val credentials = Credentials("jufeng98", value)
        PasswordSafe.instance.set(attributes, credentials)
    }

    private fun createCredentialAttributes(key: String): CredentialAttributes {
        return CredentialAttributes(generateServiceName("HttpRequest", key))
    }

}