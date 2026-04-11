package io.simplelogin.android.data.models.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import io.simplelogin.android.models.api.RandomAliasSuffix
import io.simplelogin.android.models.api.RandomMode
import io.simplelogin.android.models.api.SenderFormat
import java.lang.reflect.Type

sealed class UpdateUserSettingsOption {
    data class Notification(val value: Boolean) : UpdateUserSettingsOption()
    data class RandomModeOption(val value: RandomMode) :
        UpdateUserSettingsOption()

    data class RandomAliasDefaultDomain(val value: String) : UpdateUserSettingsOption()
    data class SenderFormatOption(val value: SenderFormat) : UpdateUserSettingsOption()
    data class RandomAliasSuffixOption(val value: RandomAliasSuffix) : UpdateUserSettingsOption()
}

class UpdateUserSettingsOptionSerializer : JsonSerializer<UpdateUserSettingsOption> {
    override fun serialize(
        src: UpdateUserSettingsOption,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonObject().apply {
            when (src) {
                is UpdateUserSettingsOption.Notification -> addProperty("notification", src.value)
                is UpdateUserSettingsOption.RandomModeOption -> addProperty(
                    "alias_generator",
                    src.value.value
                )

                is UpdateUserSettingsOption.RandomAliasDefaultDomain -> addProperty(
                    "random_alias_default_domain",
                    src.value
                )

                is UpdateUserSettingsOption.SenderFormatOption -> addProperty(
                    "sender_format",
                    src.value.value
                )

                is UpdateUserSettingsOption.RandomAliasSuffixOption -> addProperty(
                    "random_alias_suffix",
                    src.value.value
                )
            }
        }
    }
}
