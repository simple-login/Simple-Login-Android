package io.simplelogin.core.model.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

sealed class UpdateMailboxOption {
    data class Default(val value: Boolean) : UpdateMailboxOption()
    data class ChangeEmail(val value: String) : UpdateMailboxOption()
    data class CancelChangeEmail(val value: Boolean) : UpdateMailboxOption()
}

class UpdateMailboxOptionSerializer : JsonSerializer<UpdateMailboxOption> {
    override fun serialize(
        src: UpdateMailboxOption,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonObject().apply {
            when (src) {
                is UpdateMailboxOption.Default -> addProperty("default", src.value)
                is UpdateMailboxOption.ChangeEmail -> addProperty("email", src.value)
                is UpdateMailboxOption.CancelChangeEmail -> addProperty(
                    "cancel_email_change",
                    src.value
                )
            }
        }
    }
}