package org.javamaster.httpclient.mock.support

import org.javamaster.httpclient.map.MultiValueMap

interface MockSshServer {

    fun startServer(paramMap: MultiValueMap<String, String>)

    fun stopServer()

}
