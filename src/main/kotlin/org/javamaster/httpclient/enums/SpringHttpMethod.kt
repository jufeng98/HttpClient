package org.javamaster.httpclient.enums

enum class SpringHttpMethod(val qualifiedName: String, val method: HttpMethod) {
    REQUEST_MAPPING("org.springframework.web.bind.annotation.RequestMapping", HttpMethod.REQUEST),
    GET_MAPPING("org.springframework.web.bind.annotation.GetMapping", HttpMethod.GET),
    POST_MAPPING("org.springframework.web.bind.annotation.PostMapping", HttpMethod.POST),
    PUT_MAPPING("org.springframework.web.bind.annotation.PutMapping", HttpMethod.PUT),
    DELETE_MAPPING("org.springframework.web.bind.annotation.DeleteMapping", HttpMethod.DELETE),
    PATCH_MAPPING("org.springframework.web.bind.annotation.PatchMapping", HttpMethod.PATCH),
    REQUEST_PARAM("org.springframework.web.bind.annotation.RequestParam", HttpMethod.UNKNOWN),
    REQUEST_BODY("org.springframework.web.bind.annotation.RequestBody", HttpMethod.UNKNOWN),
    PATH_VARIABLE("org.springframework.web.bind.annotation.PathVariable", HttpMethod.UNKNOWN),
    REQUEST_HEADER("org.springframework.web.bind.annotation.RequestHeader", HttpMethod.UNKNOWN);

    val shortName by lazy { qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1) }

    companion object {
        private val map by lazy {
            val map = mutableMapOf<String, SpringHttpMethod>()
            for (it in entries) {
                map[it.qualifiedName] = it
            }
            map
        }

        private val shortMap by lazy {
            val map = mutableMapOf<String, SpringHttpMethod>()
            for (it in entries) {
                map[it.shortName] = it
            }
            map
        }

        fun getByQualifiedName(qualifiedName: String?): SpringHttpMethod? {
            return map[qualifiedName]
        }

        fun getByShortName(requestMapping: String?): SpringHttpMethod? {
            return shortMap[requestMapping]
        }
    }
}