package com.example.myapplication.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.android.material.button.MaterialButton

class CourseAdapter(
    var items: List<CourseItem>,
    private val onStartClick: (CourseItem) -> Unit,
    private val onCardClick: (CourseItem) -> Unit,
    private val onReviewClick: (CourseItem) -> Unit // 필요없으면 { } 전달
) : RecyclerView.Adapter<CourseAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val circle: CircleProgressView = v.findViewById(R.id.circleProgress)
        val tvTitle: TextView = v.findViewById(R.id.tvTitle)
        val tvSub: TextView   = v.findViewById(R.id.tvSub)       // item_course.xml의 '하루 목표' 텍스트
        val tvPercent: TextView = v.findViewById(R.id.tvPercent) // 오른쪽 50% 텍스트
        val btnStart: MaterialButton = v.findViewById(R.id.btnStart)
        val root: View = v
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        // 원형 진행바와 텍스트 갱신
        holder.circle.progress = item.progressPercent.coerceIn(0, 100)
        holder.tvTitle.text = item.title
        holder.tvSub.text = "예상 ${item.minutes}분 학습"  // 하단 서브 텍스트
        holder.tvPercent.text = "${item.progressPercent}%" // 오른쪽 퍼센트

        // 클릭 리스너 연결
        holder.btnStart.setOnClickListener { onStartClick(item) }
        holder.root.setOnClickListener { onCardClick(item) }
    }

    override fun getItemCount(): Int = items.size

    // 진행도 갱신 시 사용
    fun updateItems(newItems: List<CourseItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}
