package io.simplelogin.android.data.models.api

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

sealed class UpdateAliasOption {
    data class Note(val value: String) : UpdateAliasOption()
    data class Name(val value: String) : UpdateAliasOption()
    data class Mailboxes(val ids: List<Int>) : UpdateAliasOption()
    data class DisablePgp(val value: Boolean) : UpdateAliasOption()
    data class Pinned(val value: Boolean) : UpdateAliasOption()
}

class UpdateAliasOptionSerializer : JsonSerializer<UpdateAliasOption> {
    override fun serialize(
        src: UpdateAliasOption,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonObject().apply {
            when (src) {
                is UpdateAliasOption.Note -> addProperty("note", src.value)
                is UpdateAliasOption.Name -> addProperty("name", src.value)
                is UpdateAliasOption.Mailboxes -> {
                    val jsonArray = JsonArray()
                    src.ids.forEach { jsonArray.add(it) }
                    add("mailbox_ids", jsonArray)
                }

                is UpdateAliasOption.DisablePgp -> addProperty("disable_pgp", src.value)
                is UpdateAliasOption.Pinned -> addProperty("pinned", src.value)
            }
        }
    }
}
