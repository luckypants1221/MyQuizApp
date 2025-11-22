package com.example.myapplication.data.model

data class IdCheckResponse(
    val isAvailable: Boolean
)

// 스프링의 SendCodeRequest에 대응
data class SendCodeRequest(
    val email: String
)

// 스프링의 VerifyCodeRequest에 대응
// 주의: 스프링에서 request.getVerificationCode()를 쓰므로 필드명은 verificationCode여야 함
data class VerifyCodeRequest(
    val email: String,
    val verificationCode: String
)
