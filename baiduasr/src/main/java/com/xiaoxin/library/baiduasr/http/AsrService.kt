package com.xiaoxin.library.baiduasr.http

import android.util.Log
import com.xiaoxin.library.baiduasr.data.AsrResult
import com.xiaoxin.library.baiduasr.data.OAuthResult
import com.xiaoxin.library.baiduasr.http.request.AsrRequest
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

internal interface OAuthService {

    @POST("oauth/2.0/token")
    fun oauth(
        @Query("client_id") client_id: String,
        @Query("client_secret") client_secret: String,
        @Query("grant_type") grant_type: String = "client_credentials"
    ): Single<OAuthResult>

    companion object {
        const val BASE_URL = "https://openapi.baidu.com/"
    }
}

internal interface AsrService {
    @POST("server_api")
    fun asr(@Body asrRequest: AsrRequest): Single<AsrResult>

    companion object {
        const val BASE_URL = "https://vop.baidu.com/"
    }
}

internal object AsrClient {
    var connectionTimeoutInMillis: Long = 30000
    var socketTimeoutInMillis: Long = 30000

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor {
            Log.println(Log.INFO, "BaiduAsrOkHttp", it)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return@lazy OkHttpClient.Builder()
            .connectTimeout(connectionTimeoutInMillis, TimeUnit.MILLISECONDS)
            .callTimeout(socketTimeoutInMillis, TimeUnit.MILLISECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val aAuthApi by lazy {
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(OAuthService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(OAuthService::class.java)
    }

    private val asrApi by lazy {
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(AsrService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(AsrService::class.java)
    }

    @JvmStatic
    fun oAuthApi(): OAuthService = aAuthApi

    @JvmStatic
    fun asrApi(): AsrService = asrApi

}