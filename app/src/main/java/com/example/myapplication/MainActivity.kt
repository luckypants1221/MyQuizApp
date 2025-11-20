package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.auth.LoginActivity
import com.example.myapplication.auth.SignUpActivity
import com.example.myapplication.data.StudyRepository
import com.example.myapplication.ui.home.CourseAdapter
import com.example.myapplication.ui.home.CourseItem
import com.example.myapplication.ui.home.WeeklyBarChartView
import com.example.myapplication.ui.course.CourseSelectActivity
import com.example.myapplication.ui.quiz.CourseIds
import com.example.myapplication.ui.quiz.ProgressStore
import com.example.myapplication.ui.quiz.QuizActivity
import com.example.myapplication.ui.stats.MonthlyStudyActivity
import com.example.myapplication.ui.wrongnote.WrongNoteActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private val QUIZ_TOTAL = 5
    private val COURSE_ID = CourseIds.COMP_BASIC

    private var rvCourses: RecyclerView? = null
    private var rvQuests: RecyclerView? = null
    private var chartContainer: android.widget.FrameLayout? = null

    private var coursesAdapter: CourseAdapter? = null
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawer = findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawerLayout)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        navView = findViewById(R.id.navigationView)

        // 햄버거 메뉴
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_sort_by_size)
        toolbar.setNavigationOnClickListener { drawer.openDrawer(GravityCompat.START) }

        // 로그인 상태에 따라 메뉴 바꾸기
        updateSideMenu()

        // 메뉴 클릭 처리
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                R.id.action_login -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                }

                R.id.action_logout -> {
                    logoutUser()
                }

                R.id.action_monthly_study -> {
                    startActivity(Intent(this, MonthlyStudyActivity::class.java))
                }

                R.id.action_wrong_notes -> {
                    startActivity(Intent(this, WrongNoteActivity::class.java))
                }

                R.id.action_signup -> {
                    startActivity(Intent(this, SignUpActivity::class.java))
                }
            }

            drawer.closeDrawer(GravityCompat.START)
            true
        }



        // 콘텐츠 초기화
        setupHomeUI()
    }

    override fun onResume() {
        super.onResume()
        updateSideMenu()

        // 퀴즈 후 돌아오면 진행도 갱신
        val percent = loadPercent()
        val newItems = listOf(CourseItem("컴활 학습", percent, 30))
        coursesAdapter?.updateItems(newItems)
    }

    /** ---------------------------------------------------------------
     *  1) 홈 UI 초기화
     * --------------------------------------------------------------- */
    private fun setupHomeUI() {
        rvCourses = findViewById(R.id.rvCourses)
        rvQuests = findViewById(R.id.rvQuests)
        chartContainer = findViewById(R.id.chartContainer)

        rvCourses?.layoutManager = LinearLayoutManager(this)
        rvQuests?.layoutManager = LinearLayoutManager(this)

        coursesAdapter = buildCoursesAdapter(loadPercent())
        rvCourses?.adapter = coursesAdapter


// 주간 그래프 (DB와 연동 가능하게 변경)
        val chartView = WeeklyBarChartView(this).apply {

            // StudyRepository에서 주간 학습 데이터 가져오기
            values = StudyRepository.getWeeklyStudyCount(this@MainActivity)

            setPadding(24, 12, 24, 24)
        }

        chartContainer?.addView(chartView)

        findViewById<android.widget.Button>(R.id.btnSelectCourse).setOnClickListener {
            startActivity(Intent(this, CourseSelectActivity::class.java))
        }

    }

    /** ---------------------------------------------------------------
     *  2) 진행도 계산
     * --------------------------------------------------------------- */
    private fun loadPercent(): Int {
        val (_, solvedCount) = ProgressStore.load(this, COURSE_ID)
        val percent = if (QUIZ_TOTAL == 0) 0 else (solvedCount.toFloat() / QUIZ_TOTAL * 100).toInt()
        return percent.coerceIn(0, 100)
    }

    private fun buildCoursesAdapter(percent: Int): CourseAdapter {
        val courses = listOf(CourseItem("컴활 학습", percent, 30))
        return CourseAdapter(
            items = courses,
            onStartClick = {
                val (idx, solved) = ProgressStore.load(this, COURSE_ID)
                if (solved >= QUIZ_TOTAL || idx >= QUIZ_TOTAL) {
                    ProgressStore.saveSync(this, COURSE_ID, currentIndex = 1, solvedCount = 0)
                }
                startActivity(
                    Intent(this, QuizActivity::class.java)
                        .putExtra(CourseIds.EXTRA_COURSE_ID, COURSE_ID)
                )
            },
            onCardClick = {},
            onReviewClick = {}
        )
    }

    /** ---------------------------------------------------------------
     *  3) 로그인 상태 체크 + 메뉴 변경
     * --------------------------------------------------------------- */
    private fun updateSideMenu() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("isLogged_in", false)

        navView.menu.clear()

        if (isLoggedIn) {
            // 로그인 상태
            navView.inflateMenu(R.menu.menu_logged_in) // 새로 만들 메뉴 파일
        } else {
            // 비로그인 = 데모 모드
            navView.inflateMenu(R.menu.menu_demo_mode) // 새로 만들 메뉴 파일
        }
    }

    /** ---------------------------------------------------------------
     *  4) 로그아웃 기능
     * --------------------------------------------------------------- */
    private fun logoutUser() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("isLogged_in", false)
            .apply()

        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
