package com.vr.flovision.activity.admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.vr.flovision.R
import com.vr.flovision.helper.showSnack

class LoginActivity : AppCompatActivity() {
    lateinit var btnBack : ImageView
    lateinit var etUsername : EditText
    lateinit var etPassword : EditText
    lateinit var btnSimpan : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initView()
        initClick()
    }
    private fun initView(){
        btnBack = findViewById(R.id.btnBack)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnSimpan = findViewById(R.id.btnSimpan)
    }
    private fun initClick(){
        btnBack.setOnClickListener {
            finish()
        }
        btnSimpan.setOnClickListener {
            if (etUsername.text.toString().isEmpty()){
                etUsername.error = "Username tidak boleh kosong"
                etUsername.requestFocus()
            }else if (etPassword.text.toString().isEmpty()){
                etPassword.error = "Password tidak boleh kosong"
                etPassword.requestFocus()
            }else{
                if (etUsername.text.toString().equals("admin") && etPassword.text.toString().equals("admin")){
                    val intent = Intent(this, AdminActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    showSnack(this@LoginActivity,"Username atau password salah")
                }
            }
        }
    }
}