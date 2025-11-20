package com.example.myapplication.ui.stats

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.data.StudyRepository
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.appbar.MaterialToolbar

class MonthlyStudyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly_study)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarMonthly)
        toolbar.setNavigationOnClickListener { finish() }

        // 🔹 "이번 달 학습 데이터" → StudyRepository에서 가져오기
        val dailyStudyCount = StudyRepository.getMonthlyStudyCount(this)

        val studyDays = dailyStudyCount.count { it > 0 }
        val totalSolved = dailyStudyCount.sum()
        val avgPerDay = if (studyDays == 0) 0 else totalSolved / studyDays
        val mostStudyDayIndex = dailyStudyCount.indexOf(dailyStudyCount.maxOrNull() ?: 0) + 1

        // 🔹 UI 반영
        findViewById<TextView>(R.id.tvTotalSolved).text = "${totalSolved}문제"
        findViewById<TextView>(R.id.tvStudyDays).text = "${studyDays}일"
        findViewById<TextView>(R.id.tvBestDay).text = "가장 많이 공부한 날: ${mostStudyDayIndex}일"

        // 🔹 BarChart 구성
        val chart = findViewById<BarChart>(R.id.chartMonthly)
        val entries = ArrayList<BarEntry>()

        dailyStudyCount.forEachIndexed { index, value ->
            entries.add(BarEntry((index + 1).toFloat(), value.toFloat()))
        }

        val dataSet = BarDataSet(entries, "월간 학습량")
        dataSet.color = Color.parseColor("#6A5AE0")
        dataSet.valueTextColor = Color.TRANSPARENT

        val barData = BarData(dataSet)
        barData.barWidth = 0.35f

        chart.data = barData

        chart.apply {
            axisLeft.textColor = Color.GRAY
            axisRight.isEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.GRAY
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            xAxis.valueFormatter =
                com.github.mikephil.charting.formatter.IndexAxisValueFormatter(
                    dailyStudyCount.indices.map { "${it + 1}" }
                )
        }

        chart.invalidate()
    }
}
