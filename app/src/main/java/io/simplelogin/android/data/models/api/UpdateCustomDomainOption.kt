package io.simplelogin.android.data.models.api

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.annotations.SerializedName
import io.simplelogin.android.models.api.CustomDomain
import java.lang.reflect.Type

sealed class UpdateCustomDomainOption {
    data class CatchAll(val value: Boolean) : UpdateCustomDomainOption()
    data class RandomPrefixGeneration(val value: Boolean) : UpdateCustomDomainOption()
    data class Name(val value: String) : UpdateCustomDomainOption()
    data class Mailboxes(val ids: List<Int>) : UpdateCustomDomainOption()
}

class UpdateCustomDomainOptionSerializer : JsonSerializer<UpdateCustomDomainOption> {
    override fun serialize(
        src: UpdateCustomDomainOption,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonObject().apply {
            when (src) {
                is UpdateCustomDomainOption.CatchAll -> addProperty("catch_all", src.value)
                is UpdateCustomDomainOption.RandomPrefixGeneration -> addProperty(
                    "random_prefix_generation",
                    src.value
                )

                is UpdateCustomDomainOption.Name -> addProperty("name", src.value)
                is UpdateCustomDomainOption.Mailboxes -> {
                    val jsonArray = JsonArray()
                    src.ids.forEach { jsonArray.add(it) }
                    add("mailbox_ids", jsonArray)
                }
            }
        }
    }
}

data class UpdateCustomDomainResponse(
    @SerializedName("custom_domain") val customDomain: CustomDomain
)