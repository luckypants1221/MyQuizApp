package com.example.myapplication.data.model

import java.util.Date

data class Problem(
    val problemId: Long,
    val question: String,
    val answer: String,
    val problemLevvel: Int,
    val nextReviewTime: Date?
)

