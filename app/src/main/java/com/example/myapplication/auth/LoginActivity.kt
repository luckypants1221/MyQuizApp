package com.example.myapplication.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.MainActivity
import com.example.myapplication.data.remote.RetrofitClient
import com.example.myapplication.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { performLogin() }



        binding.tvGoSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun performLogin() {
        val id = binding.etId.text.toString().trim()
        val pw = binding.etPassword.text.toString().trim()

        // 1. 입력값 검사
        if (id.isEmpty() || pw.isEmpty()) {
            Toast.makeText(this, "아이디와 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. 서버로 로그인 요청 (비동기 실행)
        lifecycleScope.launch {
            try {
                // 🔥 여기서 서버에 ID, PW를 보냄 (Form Data 방식)
                val response = RetrofitClient.authApiService.login(id, pw)

                // 3. 응답 처리
                if (response.isSuccessful) {
                    // 성공 (200 OK)
                    Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()

                    // 내부 저장소에 '로그인 됨' 상태 저장
                    AuthManager.setLoggedIn(this@LoginActivity, true)

                    // 메인 화면으로 이동
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish() // 로그인 화면은 종료 (뒤로가기 눌러도 안 나오게)

                } else {
                    // 실패 (400 Bad Request 등) -> 아이디/비번 틀림
                    // 에러 메시지가 있다면 보여주기
                    val errorMsg = response.errorBody()?.string() ?: "로그인 실패"
                    Toast.makeText(this@LoginActivity, "실패: 아이디 또는 비밀번호를 확인하세요.", Toast.LENGTH_SHORT).show()
                    Log.e("LoginError", errorMsg)
                }

            } catch (e: Exception) {
                // 네트워크 오류 등 (서버 꺼짐, 인터넷 끊김)
                e.printStackTrace()
                Toast.makeText(this@LoginActivity, "통신 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
