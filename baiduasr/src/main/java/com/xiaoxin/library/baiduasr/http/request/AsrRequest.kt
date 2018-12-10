package com.xiaoxin.library.baiduasr.http.request

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.IntDef
import androidx.annotation.StringDef
import com.google.gson.annotations.SerializedName

@IntDef(
    value = [
        1536,//普通话(支持简单的英文识别)	搜索模型	无标点 支持自定义词库
        1537,//普通话(纯中文识别)	输入法模型	有标点
        1737,//英语		无标点
        1637,//英语		无标点
        1837,//四川话		有标点
        1936 //普通话远场	远场模型	有标点
    ]
)
annotation class DevPid

@StringDef(value = ["pcm", "wav", "amr"])
annotation class Format

@IntDef(value = [8000, 16000])
annotation class Rate

internal data class AsrRequest @JvmOverloads constructor(
    @SerializedName("format")
    @Format val format: String,
    @SerializedName("rate")
    @Rate val rate: Int,
    @SerializedName("channel")
    val channel: Int,
    @SerializedName("cuid")
    val cuid: String,
    @SerializedName("token")
    val token: String,
    @SerializedName("dev_pid")
    @DevPid val devPid: Int? = 1537,
    @SerializedName("speech")
    val speech: String? = null,
    @SerializedName("len")
    val len: Int? = null
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString(),
        source.readInt(),
        source.readInt(),
        source.readString(),
        source.readString(),
        source.readValue(Int::class.java.classLoader) as Int?,
        source.readString(),
        source.readValue(Int::class.java.classLoader) as Int?
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(format)
        writeInt(rate)
        writeInt(channel)
        writeString(cuid)
        writeString(token)
        writeValue(devPid)
        writeString(speech)
        writeValue(len)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<AsrRequest> = object : Parcelable.Creator<AsrRequest> {
            override fun createFromParcel(source: Parcel): AsrRequest = AsrRequest(source)
            override fun newArray(size: Int): Array<AsrRequest?> = arrayOfNulls(size)
        }
    }
}