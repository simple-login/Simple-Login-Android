package io.simplelogin.android.utils.enums

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

enum class RandomMode(val parameterName: String, val position: Int) {
    UUID("uuid", 0), WORD("word", 1);

    val description
        get() = when (this) {
            UUID -> "Based on UUID"
            WORD -> "Based on random words"
        }

    companion object {
        fun fromPosition(position: Int) =
            when {
                UUID.position == position -> UUID
                WORD.position == position -> WORD
                else -> UUID
            }
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