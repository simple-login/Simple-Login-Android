package io.simplelogin.android.models.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

sealed class UpdateUserInfoOption {
    data class DisplayName(val value: String) : UpdateUserInfoOption()
    data class ProfilePicture(val base64: String?) : UpdateUserInfoOption()
}

class UpdateUserInfoOptionSerializer : JsonSerializer<UpdateUserInfoOption> {
    override fun serialize(
        src: UpdateUserInfoOption,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonObject().apply {
            when (src) {
                is UpdateUserInfoOption.DisplayName -> addProperty("name", src.value)
                is UpdateUserInfoOption.ProfilePicture -> addProperty("profile_picture", src.base64)
            }
        }
    }
}
