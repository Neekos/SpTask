package com.example.authactadap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //-------------------------authorisation
        val etLogin = findViewById<EditText>(R.id.etLogin)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvError = findViewById<TextView>(R.id.tvLoginError)

        // внутри onCreate() LoginActivity
        val btnAdminAccess = findViewById<Button>(R.id.btnAdminAccess)
        btnAdminAccess.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val login = etLogin.text.toString()
            val password = etPassword.text.toString()
            val user = InMemoryUserRepository.getUserByLogin(login)

            if (user != null && user.password == password) {
                // Успешно: переходим на профиль
                val intent = Intent(this, UsersActivity::class.java).apply {
                    putExtra("USER_LOGIN", login)
                }
                startActivity(intent)
            } else {
                tvError.visibility = View.VISIBLE
            }
        }

        //--------------------------Registration
        val etName = findViewById<EditText>(R.id.etName)
        val etAge = findViewById<EditText>(R.id.etAge)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etLoginReg = findViewById<EditText>(R.id.etLoginReg)
        val etPasswordReg = findViewById<EditText>(R.id.etPasswordReg)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvRegisterStatus = findViewById<TextView>(R.id.tvRegisterStatus)
        val tvRegisterError = findViewById<TextView>(R.id.tvRegisterError)

        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val age = etAge.text.toString().toIntOrNull() ?: 0
            val email = etEmail.text.toString()
            val login = etLoginReg.text.toString()
            val password = etPasswordReg.text.toString()
            val user = InMemoryUserRepository.registerUser(name, age, email, login, password)

            if (user != null) {
                Toast.makeText(this, "Регистрация прошла: $login", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, UsersActivity::class.java).apply {
                    putExtra("USER_LOGIN", login)
                }
                startActivity(intent)

            } else {
                Toast.makeText(this, "Логин уже занят", Toast.LENGTH_SHORT).show()
            }
        }
    }
}



