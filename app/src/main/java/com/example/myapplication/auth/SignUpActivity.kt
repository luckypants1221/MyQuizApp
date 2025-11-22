package com.example.myapplication.auth

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.model.SendCodeRequest
import com.example.myapplication.data.model.VerifyCodeRequest
import com.example.myapplication.data.remote.RetrofitClient
import com.example.myapplication.databinding.ActivitySignUpBinding
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private var isIdChecked = false
    private var isEmailVerified = false
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupIdCheck()
        setupEmailVerification()
        setupSignUpButton()
    }

    private fun setupIdCheck() {
        binding.etId.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val loginId = binding.etId.text.toString().trim()
                if (loginId.isNotEmpty()) {
                    checkId(loginId)
                } else {
                    binding.tvIdCheckMessage.visibility = View.GONE
                }
            }
        }
    }

    private fun checkId(loginId: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authApiService.checkId(loginId)
                if (response.isSuccessful && response.body() != null) {
                    val isAvailable = response.body()!!.isAvailable
                    binding.tvIdCheckMessage.visibility = View.VISIBLE
                    if (isAvailable) {
                        binding.tvIdCheckMessage.text = "사용 가능한 아이디입니다."
                        binding.tvIdCheckMessage.setTextColor(Color.BLUE) // Success color
                        isIdChecked = true
                    } else {
                        binding.tvIdCheckMessage.text = "이미 사용 중인 아이디입니다."
                        binding.tvIdCheckMessage.setTextColor(Color.RED) // Error color
                        isIdChecked = false
                    }
                } else {
                    showToast("아이디 중복 확인 실패")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("네트워크 오류: ${e.message}")
            }
        }
    }

    private fun setupEmailVerification() {
        // 인증번호 발송
        binding.btnSendCode.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                showToast("이메일을 입력하세요.")
                return@setOnClickListener
            }
            sendEmailCode(email)
        }

        // 인증번호 재전송
        // (UI상 재전송 버튼이 따로 없고 발송 버튼을 재사용하거나 텍스트를 변경하는 방식이면 로직 조정 필요)
        // 여기서는 발송 버튼을 재사용한다고 가정 (JS 로직 참조)

        // 인증번호 확인
        binding.btnVerifyCode.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val code = binding.etEmailCode.text.toString().trim()

            if (email.isEmpty() || code.isEmpty()) {
                showToast("이메일과 인증번호를 입력하세요.")
                return@setOnClickListener
            }
            verifyEmailCode(email, code)
        }
    }

    private fun sendEmailCode(email: String) {
        binding.btnSendCode.isEnabled = false
        binding.btnSendCode.text = "발송 중..."
        binding.tvEmailMessage.text = ""
        binding.tvEmailMessage.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authApiService.sendEmailCode(SendCodeRequest(email))
                if (response.isSuccessful) {
                    val msg = response.body()?.string() ?: "인증번호가 발송되었습니다."
                    binding.tvEmailMessage.text = msg
                    binding.tvEmailMessage.setTextColor(Color.BLUE)
                    binding.tvEmailMessage.visibility = View.VISIBLE
                    
                    // 타이머 시작 (3분 = 180초)
                    startTimer(180 * 1000L)
                }
                else {
                    val errorMsg = response.errorBody()?.string() ?: "발송 실패"
                    binding.tvEmailMessage.text = errorMsg
                    binding.tvEmailMessage.setTextColor(Color.RED)
                    binding.tvEmailMessage.visibility = View.VISIBLE
                    binding.btnSendCode.isEnabled = true
                    binding.btnSendCode.text = "인증번호 발송"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvEmailMessage.text = "오류 발생: ${e.message}"
                binding.tvEmailMessage.setTextColor(Color.RED)
                binding.tvEmailMessage.visibility = View.VISIBLE
                binding.btnSendCode.isEnabled = true
                binding.btnSendCode.text = "인증번호 발송"
            }
        }
    }

    private fun startTimer(millisInFuture: Long) {
        timer?.cancel()
        timer = object : CountDownTimer(millisInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60
                val timeString = String.format("%02d:%02d", minutes, seconds)
                binding.btnSendCode.text = "재전송 ($timeString)"
                binding.btnSendCode.isEnabled = false // 타이머 도중엔 비활성화 (JS 로직 참조)
            }

            override fun onFinish() {
                binding.btnSendCode.text = "인증번호 재전송"
                binding.btnSendCode.isEnabled = true
                binding.tvEmailMessage.text = "인증 시간이 만료되었습니다. 다시 시도해주세요."
                binding.tvEmailMessage.setTextColor(Color.RED)
            }
        }.start()
    }

    private fun verifyEmailCode(email: String, code: String) {
        binding.btnVerifyCode.isEnabled = false
        binding.btnVerifyCode.text = "확인 중..."

        lifecycleScope.launch {
            try {
                val request = VerifyCodeRequest(email, code) // DTO 사용

                val response = RetrofitClient.authApiService.verifyEmailCode(request)

                if (response.isSuccessful) {
                    val msg = response.body()?.string() ?: "인증 성공"
                    binding.tvVerificationMessage.text = msg
                    binding.tvVerificationMessage.setTextColor(Color.BLUE)
                    binding.tvVerificationMessage.visibility = View.VISIBLE

                    // 성공 처리
                    isEmailVerified = true
                    binding.etEmail.isEnabled = false
                    binding.etEmailCode.isEnabled = false
                    binding.btnSendCode.isEnabled = false
                    binding.btnVerifyCode.isEnabled = false
                    timer?.cancel()
                    binding.btnSendCode.text = "인증 완료"
                    
                    checkSignUpButtonState()

                } else {
                    val errorMsg = response.errorBody()?.string() ?: "인증 실패"
                    binding.tvVerificationMessage.text = errorMsg
                    binding.tvVerificationMessage.setTextColor(Color.RED)
                    binding.tvVerificationMessage.visibility = View.VISIBLE
                    binding.btnVerifyCode.isEnabled = true
                    binding.btnVerifyCode.text = "확인"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvVerificationMessage.text = "오류 발생: ${e.message}"
                binding.tvVerificationMessage.setTextColor(Color.RED)
                binding.tvVerificationMessage.visibility = View.VISIBLE
                binding.btnVerifyCode.isEnabled = true
                binding.btnVerifyCode.text = "확인"
            }
        }
    }

    private fun setupSignUpButton() {
        binding.btnSignUp.setOnClickListener {
            // 1. 유효성 검사 (기존 코드)
            if (!isIdChecked) {
                showToast("아이디 중복 확인이 필요합니다.")
                return@setOnClickListener
            }
            if (!isEmailVerified) {
                showToast("이메일 인증이 필요합니다.")
                return@setOnClickListener
            }

            // 2. 입력값 가져오기
            val userid = binding.etId.text.toString().trim()
            val pw = binding.etPw.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val pwConfirm = binding.etPwConfirm.text.toString().trim()

            binding.btnSignUp.isEnabled = false // 중복 클릭 방지

            if (userid.isEmpty() || pw.isEmpty() || pwConfirm.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                showToast("모든 정보를 입력해주세요.")
                return@setOnClickListener
            }

            // 🔥 추가된 부분: 비밀번호 일치 검사
            if (pw != pwConfirm) {
                showToast("비밀번호가 일치하지 않습니다.")
                return@setOnClickListener
            }

            // 3. 서버 요청
            lifecycleScope.launch {
                try {
                    // DTO 객체가 아니라 파라미터를 하나씩 풀어서 보냄 (@Field 방식)
                    val response = RetrofitClient.authApiService.registerMember(
                        userid, pw, name, email, phone
                    )

                    if (response.isSuccessful) {
                        showToast("회원가입 성공! 로그인 해주세요.")
                        finish() // 가입 성공 시 화면 닫기
                    } else {
                        // 서버 에러 메시지 확인
                        val errorMsg = response.errorBody()?.string() ?: "가입 실패"
                        showToast("오류: $errorMsg")
                        binding.btnSignUp.isEnabled = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    showToast("통신 오류: ${e.message}")
                    binding.btnSignUp.isEnabled = true
                }
            }
        }
    }
    
    private fun checkSignUpButtonState() {
        // 필요 시 버튼 활성화/비활성화 로직 추가
        // 현재는 클릭 시 체크하도록 구현함
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
