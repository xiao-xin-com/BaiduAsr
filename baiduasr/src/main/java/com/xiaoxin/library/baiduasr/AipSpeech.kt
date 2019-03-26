package com.xiaoxin.library.baiduasr

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresPermission
import com.xiaoxin.library.baiduasr.data.AsrResult
import com.xiaoxin.library.baiduasr.data.OAuthResult
import com.xiaoxin.library.baiduasr.http.AsrClient
import com.xiaoxin.library.baiduasr.http.request.AsrRequest
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileInputStream
import java.net.URL

class AipSpeech(
    private val context: Context,
    private val appId: String,
    private val apiKey: String,
    private val secretKey: String
) {
    private val sp by lazy {
        context.getSharedPreferences("baidu_aip_asr", Context.MODE_PRIVATE)
    }

    companion object {
        private const val TAG = "AipSpeech"
        private const val KEY_ACCESS_TOKEN = "access_token"
    }

    private fun oauth(): Single<OAuthResult> {
        return AsrClient.oAuthApi().oauth(apiKey, secretKey)
            .doOnSuccess {
                if (sp.edit().putString(KEY_ACCESS_TOKEN, it.accessToken).commit()) {
                    Log.d(TAG, "commit access_token ${it.accessToken}")
                }
            }
    }

    private fun getAccessToken(): Single<String> {
        return Single.defer {
            val accessToken = sp.getString(KEY_ACCESS_TOKEN, null)
            if (accessToken?.isNotBlank() == true) {
                Single.just(accessToken)
            } else {
                oauth().map { it.accessToken }
            }
        }
    }

    // Return null if device ID is not available.
    @RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
    @SuppressLint("HardwareIds,MissingPermission")
    private fun cuid(): String? {
        val manager: TelephonyManager = context.applicationContext
            .getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.imei
        } else {
            manager.deviceId
        }
    }

    private fun Uri.toByteArray(): ByteArray {
        val uri = this.toString()
        val scheme = Scheme.ofUri(uri)
        return when (scheme) {
            Scheme.FILE -> {
                val path = Scheme.FILE.crop(uri)
                File(path).inputStream().use { it.readBytes() }
            }
            Scheme.HTTP, Scheme.HTTPS -> {
                URL(uri).openStream()?.buffered()?.use { it.readBytes() }
            }
            else -> byteArrayOf()
        } ?: byteArrayOf()
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
    fun asr(uri: Uri, format: String, rate: Int): Single<AsrResult> {
        return Single.fromCallable { uri.toByteArray() }.flatMap { asr(it, format, rate) }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
    fun asr(path: String, format: String, rate: Int): Single<AsrResult> {
        return Single.fromCallable { FileInputStream(path).readBytes() }
            .flatMap { asr(it, format, rate) }
    }

    @RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
    @SuppressLint("HardwareIds,MissingPermission")
    fun asr(data: ByteArray, format: String, rate: Int): Single<AsrResult> {
        return getAccessToken().map { accessToken ->
            asrRequest(accessToken, data, format, rate)
        }.flatMap(this::asr).retryWhen {
            it.filter { throwable ->
                throwable is TokenIllegalException
            }.delaySubscription(oauth().toFlowable())
        }.subscribeOn(Schedulers.io())
    }

    @RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
    @SuppressLint("HardwareIds,MissingPermission")
    private fun asrRequest(
        accessToken: String,
        data: ByteArray,
        format: String,
        rate: Int
    ): AsrRequest {
        val speech = Base64.encodeToString(data, Base64.NO_WRAP) ?: ""
        return AsrRequest(
            format = format,
            rate = rate,
            channel = 1,
            cuid = cuid() ?: "default",
            devPid = 1537,
            token = accessToken,
            speech = speech,
            len = data.size
        )
    }

    private class TokenIllegalException(message: String) : Exception(message)

    private fun asr(asrRequest: AsrRequest): Single<AsrResult> {
        return AsrClient.asrApi().asr(asrRequest).doOnSuccess {
            if (it.errNo == 3302) {
                throw TokenIllegalException("token字段校验失败。请用appkey 和 app secret生成")
            }
        }
    }

    fun setConnectionTimeoutInMillis(timeout: Long) {
        AsrClient.connectionTimeoutInMillis = timeout
    }

    fun setSocketTimeoutInMillis(timeout: Long) {
        AsrClient.socketTimeoutInMillis = timeout
    }
}