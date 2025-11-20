package com.example.myapplication.ui.course

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.ui.home.CourseItem
import com.example.myapplication.ui.quiz.CourseIds
import com.example.myapplication.ui.quiz.QuizActivity

class CourseSelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_select)

        val rv = findViewById<RecyclerView>(R.id.rvCourseSelect)
        rv.layoutManager = LinearLayoutManager(this)

        val items = listOf(
            CourseItem("컴활 1급 필기", 0, 50),
            CourseItem("컴활 2급 필기", 0, 40),
            CourseItem("정보처리기사", 0, 60),
            CourseItem("파이썬 기초", 0, 35),
        )

        rv.adapter = CourseSelectAdapter(items) { selected ->
            // 버튼 클릭 시 해당 과목으로 퀴즈 시작
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra(CourseIds.EXTRA_COURSE_ID, selected.title)
            startActivity(intent)
        }
    }
}
