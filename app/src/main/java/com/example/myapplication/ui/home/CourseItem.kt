package com.example.myapplication.ui.home

data class CourseItem(
    val title: String,
    val progressPercent: Int, // 0~100
    val minutes: Int          // 예상 학습 시간 등
)
