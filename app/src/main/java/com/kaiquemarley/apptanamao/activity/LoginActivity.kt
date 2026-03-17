package com.kaiquemarley.apptanamao.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kaiquemarley.apptanamao.R
import com.kaiquemarley.apptanamao.dao.AppDataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var db: AppDataBase
    private lateinit var etEmailLogin: EditText
    private lateinit var etSenhaLogin: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = AppDataBase.instancia(this)

        etEmailLogin = findViewById(R.id.etEmailLogin)
        etSenhaLogin = findViewById(R.id.etSenhaLogin)
        val btnEntrar = findViewById<Button>(R.id.btnEntrar)
        val btnCadastroTrabalhador = findViewById<Button>(R.id.btnCadastroTrabalhador)
        val btnCadastroUsuario = findViewById<Button>(R.id.btnCadastroUsuario)

        btnEntrar.setOnClickListener {
            val email = etEmailLogin.text.toString().trim()
            val senha = etSenhaLogin.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                autenticarUsuario(email, senha)
            }
        }

        btnCadastroTrabalhador.setOnClickListener {
            val intent = Intent(this, CadastroTrabalhadorActivity::class.java)
            startActivity(intent)
        }

        btnCadastroUsuario.setOnClickListener {
            val intent = Intent(this, CadastroUsuarioActivity::class.java)
            startActivity(intent)
        }
    }

    private fun autenticarUsuario(email: String, senha: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val trabalhador = db.trabalhadorDao().autenticar(email, senha)

                if (trabalhador != null) {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Login como Trabalhador realizado com sucesso!", Toast.LENGTH_SHORT).show()
                        limparCampos()
                        val intent = Intent(this@LoginActivity, PerfilTrabalhadorActivity::class.java)
                        intent.putExtra("EMAIL", email)
                        startActivity(intent)
                    }
                } else {
                    val usuario = db.usuarioDao().getUsuarioPorEmail(email)

                    if (usuario != null && usuario.senha == senha) {
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Login como Usuário realizado com sucesso!", Toast.LENGTH_SHORT).show()
                            limparCampos()
                            val intent = Intent(this@LoginActivity, ListaTrabalhadorActivity::class.java)
                            intent.putExtra("EMAIL", email)
                            startActivity(intent)
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Email ou senha incorretos", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Erro ao autenticar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun limparCampos() {
        etEmailLogin.text?.clear()
        etSenhaLogin.text?.clear()
    }
}