package com.xiaoxin.library.baiduasr.data

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class OAuthResult(
    @SerializedName("access_token")
    val accessToken: String? = null,
    @SerializedName("expires_in")
    val expiresIn: Long? = null,
    @SerializedName("refresh_token")
    val refreshToken: String? = null,
    @SerializedName("scope")
    val scope: String? = null,
    @SerializedName("session_key")
    val sessionKey: String? = null,
    @SerializedName("session_secret")
    val sessionSecret: String? = null
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString(),
        source.readValue(Long::class.java.classLoader) as Long?,
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(accessToken)
        writeValue(expiresIn)
        writeString(refreshToken)
        writeString(scope)
        writeString(sessionKey)
        writeString(sessionSecret)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<OAuthResult> = object : Parcelable.Creator<OAuthResult> {
            override fun createFromParcel(source: Parcel): OAuthResult = OAuthResult(source)
            override fun newArray(size: Int): Array<OAuthResult?> = arrayOfNulls(size)
        }
    }
}