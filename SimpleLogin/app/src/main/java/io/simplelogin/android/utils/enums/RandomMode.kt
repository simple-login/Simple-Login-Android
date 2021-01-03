package io.simplelogin.android.utils.enums

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

enum class RandomMode(val parameterName: String) {
    UUID("uuid"), WORD("word");

    val description
        get() = when (this) {
            RandomMode.UUID -> "Based on UUID"
            RandomMode.WORD -> "Based on random words"
        }
}

class RandomModeDeserializer : JsonDeserializer<RandomMode> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): RandomMode =
        when (json?.asString) {
            "word" -> RandomMode.WORD
            else -> RandomMode.UUID
        }
}