package com.yoohayoung.youhi

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "response", strict = false)
data class ApiResponse(
    @field:Element(name = "protocol", required = false)
    var protocol: String = "",

    @field:Element(name = "code", required = false)
    var code: Int = 0,

    @field:Element(name = "message", required = false)
    var message: String = "",

    @field:Element(name = "url", required = false)
    var url: String = ""
)