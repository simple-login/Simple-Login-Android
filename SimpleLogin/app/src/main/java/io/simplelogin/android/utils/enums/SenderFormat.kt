package io.simplelogin.android.utils.enums

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

enum class SenderFormat(val parameterName: String) {
    A("A"), AT("AT"), FULL("FULL"), VIA("VIA");

    val description
        get() = when (this) {
            A -> "John Doe - john.doe(a)example.com"
            AT -> "John Doe - john.doe at example.com"
            FULL -> "John Doe - john.doe@example.com"
            VIA -> "john.doe@example.com via SimpleLogin"
        }
}

class SenderFormatDeserializer : JsonDeserializer<SenderFormat> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): SenderFormat =
        when (json?.asString) {
            "AT" -> SenderFormat.AT
            "FULL" -> SenderFormat.FULL
            "VIA" -> SenderFormat.VIA
            else -> SenderFormat.A
        }
}