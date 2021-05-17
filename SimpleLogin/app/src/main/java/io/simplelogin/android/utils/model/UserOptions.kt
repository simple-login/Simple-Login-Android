package io.simplelogin.android.utils.model

import com.google.gson.annotations.SerializedName

data class UserOptions(
    @SerializedName("can_create") val canCreate: Boolean,
    @SerializedName("suffixes") val suffixes: List<Suffix>
)

data class Suffix(
    @SerializedName("suffix") val suffix: String,
    @SerializedName("signed_suffix") val signedSuffix: String
)

/*
{
  "can_create": true,
  "prefix_suggestion": "test",
  "suffixes": [
    {
      "signed_suffix": ".cat@d1.test.X6_7OQ.0e9NbZHE_bQvuAapT6NdBml9m6Q",
      "suffix": ".cat@d1.test"
    },
    {
      "signed_suffix": ".chat@d2.test.X6_7OQ.TTgCrfqPj7UmlY723YsDTHhkess",
      "suffix": ".chat@d2.test"
    },
    {
      "signed_suffix": ".yeah@sl.local.X6_7OQ.i8XL4xsMsn7dxDEWU8eF-Zap0qo",
      "suffix": ".yeah@sl.local"
    }
  ]
}
*/
