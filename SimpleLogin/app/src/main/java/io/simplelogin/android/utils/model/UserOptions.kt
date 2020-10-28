package io.simplelogin.android.utils.model

import com.google.gson.annotations.SerializedName

data class UserOptions(
    @SerializedName("can_create") val canCreate: Boolean,
    @SerializedName("suffixes") val suffixes: List<List<String>>
)

/*
{
    "can_create": true,
    "prefix_suggestion": "",
    "suffixes": [
    [
    ".claustrum@sldev.ovh",
    ".claustrum@sldev.ovh.XtTG1w.N_0x77e2dOYlCklEM1RaOp0q3Fc"
    ],
    [
    ".cellulosing@hai.sldev.ovh",
    ".cellulosing@hai.sldev.ovh.XtTG1w.dY3zGAGRU9MV7LhBKVMfgFEWNrA"
    ]
    ]
}
*/
