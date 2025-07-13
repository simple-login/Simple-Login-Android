package io.simplelogin.android.data.models.api

import com.google.gson.annotations.SerializedName
import java.util.UUID
import kotlin.random.Random

data class Alias(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String?,
    @SerializedName("enabled") val enabled: Boolean,
    @SerializedName("creation_timestamp") val creationTimestamp: Double,
    @SerializedName("nb_block") val blockCount: Int,
    @SerializedName("nb_forward") val forwardCount: Int,
    @SerializedName("nb_reply") val replyCount: Int,
    @SerializedName("note") val note: String?,
    @SerializedName("support_pgp") val pgpSupported: Boolean,
    @SerializedName("disable_pgp") val pgpDisabled: Boolean,
    @SerializedName("mailboxes") val mailboxes: List<MailboxLite>,
    @SerializedName("latest_activity") val latestActivity: LatestActivity?,
    @SerializedName("pinned") val pinned: Boolean
) {
    data class LatestActivity(
        val action: ActivityAction,
        val contact: Contact,
        val timestamp: Double
    ) {
        data class Contact(
            @SerializedName("email") val email: String,
            @SerializedName("name") val name: String?,
            @SerializedName("reverse_alias") val reverseAlias: String
        )
    }

    val hasActivities: Boolean = (forwardCount + replyCount + blockCount) > 0

    // Break just before "@" to make multi-lines emails more readable
    val breakableEmail = email.replace("@", "\u200B@")
}

data class Aliases(
    @SerializedName("aliases") val aliases: List<Alias>
)

fun generateRandomAlias(id: Int = Random.nextInt()): Alias {
    val randomDomain = listOf("example.com", "mailbox.org", "test.net").random()
    val randomName = listOf("alpha", "bravo", "charlie", "delta").random()
    val email = "$randomName$id@$randomDomain"

    return Alias(
        id = id,
        email = email,
        name = randomName.replaceFirstChar { it.uppercase() },
        enabled = Random.nextBoolean(),
        creationTimestamp = System.currentTimeMillis() / 1000.0,
        blockCount = Random.nextInt(0, 10),
        forwardCount = Random.nextInt(0, 10),
        replyCount = Random.nextInt(0, 10),
        note = listOf("Important", "Work", "Temporary", null).random(),
        pgpSupported = Random.nextBoolean(),
        pgpDisabled = Random.nextBoolean(),
        mailboxes = listOf(generateRandomMailboxLite()),
        latestActivity = generateRandomLatestActivity(),
        pinned = Random.nextBoolean()
    )
}

fun generateRandomLatestActivity(): Alias.LatestActivity {
    return Alias.LatestActivity(
        action = ActivityAction.entries.toTypedArray().random(),
        contact = Alias.LatestActivity.Contact(
            email = "contact${Random.nextInt(100)}@domain.com",
            name = listOf("Alice", "Bob", "Charlie", null).random(),
            reverseAlias = "reverse-${UUID.randomUUID()}"
        ),
        timestamp = System.currentTimeMillis() / 1000.0
    )
}

// Example for MailboxLite generator
fun generateRandomMailboxLite(): MailboxLite {
    return MailboxLite(
        id = Random.nextInt(),
        email = "mailbox${Random.nextInt(1000)}@domain.com"
    )
}