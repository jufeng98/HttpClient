package org.javamaster.httpclient.utils

import java.io.FileInputStream
import java.net.Socket
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedTrustManager


/**
 * @author yudong
 */
object SslUtil {

    private val trustAllCerts: Array<X509ExtendedTrustManager> by lazy {
        val trustManager = object : X509ExtendedTrustManager() {
            override fun checkClientTrusted(chain: Array<out X509Certificate?>?, authType: String?, socket: Socket?) {
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate?>?, authType: String?, socket: Socket?) {
            }

            override fun checkClientTrusted(
                chain: Array<out X509Certificate?>?,
                authType: String?,
                engine: SSLEngine?,
            ) {
            }

            override fun checkServerTrusted(
                chain: Array<out X509Certificate?>?,
                authType: String?,
                engine: SSLEngine?,
            ) {
            }

            override fun checkClientTrusted(
                chain: Array<out X509Certificate?>?,
                authType: String?,
            ) {
            }

            override fun checkServerTrusted(
                chain: Array<out X509Certificate?>?,
                authType: String?,
            ) {
            }

            override fun getAcceptedIssuers(): Array<out X509Certificate?> {
                return arrayOf()
            }
        }

        arrayOf(trustManager)
    }

    fun trustAllCert(): SSLContext {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())
        return sslContext
    }

    fun clientP12Cert(keyStorePath: String, keyStorePassword: String?): SSLContext {
        val keyStore = KeyStore.getInstance("PKCS12")
        FileInputStream(keyStorePath).use {
            keyStore.load(it, keyStorePassword?.toCharArray())
        }

        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(keyStore, keyStorePassword?.toCharArray())

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(kmf.keyManagers, null, null)

        return sslContext
    }
}
