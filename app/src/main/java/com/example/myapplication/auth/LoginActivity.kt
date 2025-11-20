package com.example.myapplication.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.MainActivity
import com.example.myapplication.databinding.ActivityLoginBinding

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

        if (id.isEmpty() || pw.isEmpty()) {
            Toast.makeText(this, "아이디/비번을 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO 🔥 서버 연결 지점
        // ApiClient.authApi.login(LoginRequest(id, pw))
// 로그인 성공 시
        AuthManager.setLoggedIn(this, true)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
