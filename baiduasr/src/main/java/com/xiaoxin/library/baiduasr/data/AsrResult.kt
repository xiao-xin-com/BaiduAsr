package com.xiaoxin.library.baiduasr.data

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class AsrResult(
    @SerializedName("corpus_no")
    val corpusNo: String? = null,
    @SerializedName("err_msg")
    val errMsg: String? = null,
    @SerializedName("err_no")
    val errNo: Int? = null,
    @SerializedName("result")
    val result: List<String>? = null,
    @SerializedName("sn")
    val sn: String? = null
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString(),
        source.readString(),
        source.readValue(Int::class.java.classLoader) as Int?,
        ArrayList<String>().apply { source.readList(this, String::class.java.classLoader) },
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(corpusNo)
        writeString(errMsg)
        writeValue(errNo)
        writeList(result)
        writeString(sn)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<AsrResult> = object : Parcelable.Creator<AsrResult> {
            override fun createFromParcel(source: Parcel): AsrResult = AsrResult(source)
            override fun newArray(size: Int): Array<AsrResult?> = arrayOfNulls(size)
        }
    }
}