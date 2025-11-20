package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class SubmissionRequest(
    val problemId: Long,
    val userAnswer: String,
    val checkCount: Int
)