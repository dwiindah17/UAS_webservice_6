package com.example.batik.api.response

import com.google.gson.annotations.SerializedName

data class Default (
    @SerializedName("Nama")
    var nama:String?,
    @SerializedName("Deskripsi")
    var deskripsi: String?,
    @SerializedName("Ciri")
    var ciri:String?,
    @SerializedName("Accuracy")
    var accuracy:String?
)