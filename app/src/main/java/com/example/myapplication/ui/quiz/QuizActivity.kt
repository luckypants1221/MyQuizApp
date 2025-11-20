package com.example.myapplication.ui.quiz

import android.content.ContentValues.TAG
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.R
import com.example.myapplication.data.model.Problem
import com.example.myapplication.data.model.SubmissionResponse
import com.example.myapplication.ui.viewmodel.ProblemViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator


class QuizActivity : AppCompatActivity() {

    private val problemViewModel : ProblemViewModel by viewModels()
    private var actualProblems: List<Problem> = emptyList()
    private val total get() = actualProblems.size

    // 뷰
    private lateinit var progress: LinearProgressIndicator
    private lateinit var tvPercent: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var etAnswerInput : android.widget.EditText
    private lateinit var btnSubmit: MaterialButton

    private lateinit var explanationContainer: View
    private lateinit var tvExplanation: TextView
    private lateinit var feedbackBar: View
    private lateinit var tvFeedback: TextView
    private lateinit var btnContinue: MaterialButton
    private var skipAutoSave = false
    private lateinit var ivJudge: android.widget.ImageView

    private lateinit var btnHint: MaterialButton
    private lateinit var tvHintContent: TextView

    private var current = 1
    private var answered = false
    private var isCorrect = false
    private lateinit var courseId: String

    private var hintCount = 0

    private var hasUserInput: Boolean = false

    private var currentHintText: CharSequence? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        courseId = intent.getStringExtra(CourseIds.EXTRA_COURSE_ID) ?: CourseIds.COMP_BASIC

        bindViews()
        setupProgress()
        observeViewModel()

        problemViewModel.fetchProblems(courseId)

        val (savedIndex, savedSolved) = ProgressStore.load(this, courseId)
        if (savedSolved >= total || savedIndex >= total) {
            current = 1
            answered = false
            ProgressStore.save(this, courseId, currentIndex = 1, solvedCount = 0)
        } else {
            current = savedIndex.coerceAtLeast(1 )
        }

        onBackPressedDispatcher.addCallback(this) { showExitConfirmDialog() }
    }


    private fun observeViewModel() {
        problemViewModel.allProblemsLiveData.observe(this) { problems ->
            if (problems.isNotEmpty()) {
                actualProblems = problems
                Log.i(TAG, "서버에서 ${problems.size}개의 문제 수신 및 UI 초기화 시작")

                setupProgress()
                renderQuestion()
                updateProgress()
                Log.i(TAG,"renderQuestion() 호출 완료. tvQuestion 텍스트 설정 시도")
            } else {
                Log.w(TAG, "수신된 문제 목록이 비어 있습니다.")
            }
        }

        problemViewModel.submissionResult.observe(this) { result ->
            Log.d(TAG, "$result.isCorrect")
            if (result != null) {
                isCorrect = result.isCorrect
                renderSubmitResult(result.isCorrect, result.updatedProblem)
                // ViewModel에서 LiveData 초기화를 처리하는 것을 권장
            } else if (answered) {
                // 이전에 답변을 제출했지만 결과가 null로 왔을 때 (통신 오류 등)
                Log.e(TAG, "문제 제출 결과 수신 실패")
            }
        }

        problemViewModel.hintContent.observe(this) { hint ->
            var fullHint: String? = null
            if(hintCount == 1){
                fullHint = if (!hint.isNullOrEmpty()) "정답은 ${hint}글자로 되어 있어요." else null
            }else if(hintCount > 1){
                fullHint = if (!hint.isNullOrEmpty()) "$hint" else null
            }

            currentHintText = fullHint
            Log.d(TAG, "UI 관찰: 수신된 힌트: $hint")

            if (!fullHint.isNullOrEmpty()) {
                etAnswerInput.hint = fullHint
            } else {
                etAnswerInput.hint = null
            }

            if (hintCount >= 3) {
                btnHint.isEnabled = false
                btnHint.text = "힌트 사용 완료"
            }
        }

        problemViewModel.errorMessage.observe(this){message ->
            if(message.isNotEmpty()){
                Log.e(TAG, "에러 발생:$message")
            }
        }
    }

    private fun bindViews() {

        etAnswerInput = findViewById(R.id.etAnswerInput)

        btnSubmit = findViewById(R.id.btnSubmit)

        progress = findViewById(R.id.progressQuiz)
        tvPercent = findViewById(R.id.tvProgressPercent)
        tvQuestion = findViewById(R.id.tvQuestion)

        ivJudge = findViewById(R.id.ivJudge)

        explanationContainer = findViewById(R.id.explanationContainer)
        tvExplanation = findViewById(R.id.tvExplanation)
        feedbackBar = findViewById(R.id.feedbackBar)
        tvFeedback = findViewById(R.id.tvFeedback)
        btnContinue = findViewById(R.id.btnContinue)

        btnHint = findViewById(R.id.btnHint)

        bindHintClick()

        bindSubmitClick()

        btnContinue.setOnClickListener {
            goToNextProblem()
        }


        etAnswerInput.setOnEditorActionListener { _, actionId, event ->

            val isEnterAction = actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.keyCode == android.view.KeyEvent.KEYCODE_ENTER && event.action == android.view.KeyEvent.ACTION_DOWN)

            if (isEnterAction) {

                val imm = ContextCompat.getSystemService(this, android.view.inputmethod.InputMethodManager::class.java)
                imm?.hideSoftInputFromWindow(etAnswerInput.windowToken, 0)

                if (answered) {
                    goToNextProblem()
                } else {
                    submitCurrentAnswer()
                }

                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }
    private fun goToNextProblem() {
        if (!answered) return

        if (current < total) {
            current += 1
            hideFeedbacks()
            renderQuestion()
            updateProgress()
            btnContinue.text = if (current == total) "완료" else "다음 문제"

            ProgressStore.save(this, courseId, currentIndex = current, solvedCount = solvedSoFar())
        } else {
            progress.setProgressCompat(total, true)
            tvPercent.text = "100%"
            showCompletion()
        }
    }

    private fun setupProgress() {
        progress.max = total
        updateProgress()
        btnContinue.text = if (current == total) "완료" else "다음 문제"
    }

    private fun solvedSoFar(): Int {
        val base = (current - 1).coerceAtLeast(0) // 이전까지 완전히 끝낸 개수
        val extra = if (answered) 1 else 0 // 현재 문제를 이미 풀었다면 +1
        return (base + extra).coerceAtMost(total)
    }

    private fun updateProgress() {
        val solved = solvedSoFar()
        progress.setProgressCompat(solved, true)
        val pct = if (total == 0) 0 else (solved.toFloat() / total * 100).toInt()
        tvPercent.text = "$pct%"
    }

    private fun bindSubmitClick(){
        btnSubmit.setOnClickListener {
            // ✅ 분리된 제출 로직 함수 호출
            submitCurrentAnswer()
        }
    }

    private fun submitCurrentAnswer() {
        if (answered) return

        val userAnswer = etAnswerInput.text.toString().trim()

        val currentProblem = actualProblems.getOrNull(current - 1) ?: run {
            Log.e(TAG, "현재 문제 없음")
            return
        }

        Log.d(TAG, "제출 데이터 확인: ProblemID=${currentProblem.problemId}, Answer='$userAnswer'")

        if (userAnswer.isBlank()) {
            tvFeedback.text = "답변을 입력해주세요."
            feedbackBar.visibility = View.VISIBLE
            return
        }

        val checkCount = 0

        // ✅ 서버에 답변 제출 요청
        problemViewModel.submitAnswer(currentProblem.problemId, userAnswer, checkCount)
    }


    private fun renderQuestion() {
        val item = actualProblems.getOrNull(current - 1) ?: run {
            Log.e(TAG, "문제 리스트 로드 전이거나 인덱스 오류 발생 : $current")
            return
        }

        tvQuestion.text = item.question
        Log.d(TAG, "질문 텍스트 설정 완료: \"${item.question}")

        etAnswerInput.setText("")
        etAnswerInput.isEnabled = true
        answered = false
        isCorrect = false
        hideFeedbacks()

        hintCount = 0
        currentHintText = null

        btnHint.isEnabled = true
        btnHint.text = "힌트 보기"

        problemViewModel.clearHintData()

        ivJudge.setImageResource(R.drawable.quit2)
        btnContinue.text = if (current == total) "완료" else "다음 문제"

        // ✅ 추가된 코드: 버튼 상태를 '제출하기' 모드로 초기화
        btnSubmit.visibility = View.VISIBLE
        btnContinue.visibility = View.GONE
    }
    private fun hideFeedbacks() {
        explanationContainer.visibility = View.GONE
        tvExplanation.text = ""
        feedbackBar.visibility = View.GONE
        tvFeedback.text = ""
    }

    private fun showExitConfirmDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("퀴즈 나가기")
            .setMessage("나가면 진행 상황이 저장돼요. 나갈까요?")
            .setNegativeButton("취소") { d, _ -> d.dismiss() }
            .setPositiveButton("나가기") { d, _ ->
                d.dismiss()
                ProgressStore.saveSync(
                    this, courseId,
                    currentIndex = current,
                    solvedCount = solvedSoFar()
                )
                finish()
            }
            .show()
    }

    private fun showCompletion() {
        MaterialAlertDialogBuilder(this)
            .setTitle("퀴즈 완료")
            .setMessage("모든 문제를 다 풀었어요! 처음부터 다시 풀까요?")
            .setNegativeButton("닫기") { d, _ ->
                d.dismiss()
                skipAutoSave = true
                ProgressStore.saveSync(this, courseId, currentIndex = total, solvedCount = total)
                finish()
            }
            .setPositiveButton("다시 풀기") { d, _ ->
                d.dismiss()
                skipAutoSave = false
                current = 1
                answered = false
                ProgressStore.save(this, courseId, currentIndex = 1, solvedCount = 0)

                hideFeedbacks()
                renderQuestion()
                updateProgress() // 0%
                btnContinue.text = "다음 문제"
            }
            .show()
    }

    private fun renderSubmitResult(isCorrect: Boolean, updatedProblem: Problem?) {

        feedbackBar.visibility = View.VISIBLE

        if (isCorrect == true) {
            answered = true
            etAnswerInput.isEnabled = false

            btnSubmit.visibility = View.GONE
            btnContinue.visibility = View.VISIBLE

            val reviewTime = updatedProblem?.nextReviewTime

            tvFeedback.text = if (reviewTime != null) {
                "정답이에요! 다음 복습 시간: $reviewTime"
            } else {
                "정답이에요!"
            }

            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.brand_primary))
            ivJudge.setImageResource(R.drawable.quit3)
            explanationContainer.visibility = View.GONE
        } else {
            answered = false
            etAnswerInput.isEnabled = true

            btnSubmit.visibility = View.VISIBLE
            btnContinue.visibility = View.GONE

            tvFeedback.text = "아쉽다! 오답이에요."
            tvFeedback.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))

            explanationContainer.visibility = View.VISIBLE
            ivJudge.setImageResource(R.drawable.quit4)
        }
        ivJudge.visibility = View.VISIBLE
    }

    private fun bindHintClick() {
        btnHint.setOnClickListener {
            if (answered) return@setOnClickListener

            etAnswerInput.setText("")

            hintCount += 1
            Log.d(TAG, "힌트 버튼 클릭, 힌트 카운트: $hintCount")

            val currentProblem = actualProblems.getOrNull(current - 1) ?: run {
                Log.e(TAG, "힌트 요청: 현재 문제 없음")
                return@setOnClickListener
            }

            problemViewModel.requestHint(currentProblem.problemId, hintCount)
        }
    }

    override fun onPause() {
        super.onPause()
        if (skipAutoSave) return
        ProgressStore.save(this, courseId, currentIndex = current, solvedCount = solvedSoFar())
    }



}