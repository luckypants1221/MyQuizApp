package com.example.myapplication.data.remote

import com.example.myapplication.data.model.IdCheckResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {
    @GET("api/members/check-id")
    suspend fun checkId(@Query("loginId") loginId: String): Response<IdCheckResponse>

    @FormUrlEncoded
    @POST("api/email/send-code")
    suspend fun sendEmailCode(@Field("email") email: String): Response<ResponseBody>

    @FormUrlEncoded
    @POST("api/email/verify-code")
    suspend fun verifyEmailCode(
        @Field("email") email: String,
        @Field("code") code: String
    ): Response<ResponseBody>
}
