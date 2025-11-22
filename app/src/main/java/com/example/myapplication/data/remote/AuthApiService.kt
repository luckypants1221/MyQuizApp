package com.example.myapplication.data.remote

import com.example.myapplication.data.model.IdCheckResponse
import com.example.myapplication.data.model.SendCodeRequest
import com.example.myapplication.data.model.VerifyCodeRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {
    @GET("api/members/check-id")
    suspend fun checkId(@Query("loginId") loginId: String): Response<IdCheckResponse>

    // 2. 이메일 발송 (FormUrlEncoded 제거 -> Body 사용)
    @POST("api/email/send-code")
    suspend fun sendEmailCode(@Body request: SendCodeRequest): Response<ResponseBody>

    // 3. 이메일 검증 (FormUrlEncoded 제거 -> Body 사용)
    @POST("api/email/verify-code")
    suspend fun verifyEmailCode(@Body request: VerifyCodeRequest): Response<ResponseBody>

    @FormUrlEncoded // 중요: @ModelAttribute는 이 방식이어야 함!
    @POST("/register-process")
    suspend fun registerMember(
        @Field("userid") userid: String,
        @Field("pw") pw: String,
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("phone") phone: String
    ): Response<ResponseBody> // 서버가 String("redirect:/")을 반환하므로 ResponseBody로 받음

    @FormUrlEncoded
    @POST("/api/login")
    suspend fun login(
        @Field("userid") userid: String,
        @Field("pw") pw: String
    ): Response<ResponseBody>
}
