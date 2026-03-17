package com.kaiquemarley.apptanamao.activity

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.kaiquemarley.apptanamao.dao.AppDataBase
import com.kaiquemarley.apptanamao.databinding.ActivityCadastroUsuarioBinding
import com.kaiquemarley.apptanamao.model.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class CadastroUsuarioActivity : AppCompatActivity() {


    private lateinit var binding: ActivityCadastroUsuarioBinding
    private lateinit var db: AppDataBase

    private val estados = listOf(
        "Selecione seu estado",
        "AC", "AL", "AM", "AP", "BA", "CE", "DF", "ES", "GO",
        "MA", "MG", "MS", "MT", "PA", "PB", "PE", "PI", "PR",
        "RJ", "RN", "RO", "RR", "RS", "SC", "SE", "SP", "TO"
    )


    private fun aplicarMascara(editText: android.widget.EditText, mask: String) {
        var isUpdating = false
        var oldText = ""

        editText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

            override fun afterTextChanged(s: android.text.Editable?) {
                if (isUpdating) return

                val str = s.toString().filter { it.isDigit() }

                var mascaraAplicada = ""
                var i = 0

                for (m in mask) {
                    if (m != '#') {
                        mascaraAplicada += m
                    } else {
                        if (i >= str.length) break
                        mascaraAplicada += str[i]
                        i++
                    }
                }

                isUpdating = true
                editText.setText(mascaraAplicada)
                editText.setSelection(mascaraAplicada.length)
                isUpdating = false
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDataBase.instancia(this)

        binding.btnSalvar.setOnClickListener {
            salvarUsuario()
        }

        binding.etTelefone.doOnTextChanged { text, _, _, _ ->
            val telefone = text.toString()
            binding.tvTelefoneAviso.visibility =
                if (telefone.length in 1..14) View.VISIBLE else View.GONE
        }

        // Validação de senha enquanto digita
        binding.etSenha.doOnTextChanged { text, _, _, _ ->
            val senha = text.toString()
            binding.tvSenhaAviso.visibility = if (senha.length in 1..5) {
                View.VISIBLE
            } else {
                View.GONE

            }

            binding.etTelefone.doOnTextChanged { text, _, _, _ ->
                val telefone = text.toString()
                binding.tvTelefoneAviso.visibility =
                    if (telefone.length in 1..14) View.VISIBLE else View.GONE
            }

        }

        // Máscara para telefone: (00) 00000-0000
        aplicarMascara(binding.etTelefone, "(##) #####-####")

        val estadoAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, estados)
        estadoAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerEstado.adapter = estadoAdapter
    }



    private fun salvarUsuario() {
        val nome = binding.etNome.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val senha = binding.etSenha.text.toString().trim()
        val estado = binding.spinnerEstado.selectedItem.toString()
        val cidade = binding.etCidade.text.toString().trim()
        val endereco = binding.etEndereco.text.toString().trim()
        val telefone = binding.etTelefone.text.toString().trim()
        val confirmarSenha = binding.etConfirmarSenha.text.toString().trim()

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || endereco.isEmpty()  || cidade.isEmpty() || telefone.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        if (senha.length < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!email.endsWith("@gmail.com")) {
            Toast.makeText(this, "O email deve terminar com @gmail.com", Toast.LENGTH_SHORT).show()
            return
        }

        if (telefone.length < 15) {
            Toast.makeText(this, "Telefone inválido. Deve ter o formato (00) 00000-0000.", Toast.LENGTH_SHORT).show()
            return
        }

        if (senha != confirmarSenha) {
            Toast.makeText(this, "As senhas não coincidem.", Toast.LENGTH_SHORT).show()
            return
        }
            if (estado == "Selecione seu estado") {
                Toast.makeText(this, "Por favor, selecione um estado.", Toast.LENGTH_SHORT).show()
                return

        }
        //verifica se o email que esta no cadastro é de um trbalhador ou usuario, caso seja, o sistema nao deixa cadastrar um email existente
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val usuarioExistente = db.usuarioDao().getUsuarioPorEmail(email)
                val trabalhadorExistente = db.trabalhadorDao().getTrabalhadorPorEmail(email)

                if (usuarioExistente != null || trabalhadorExistente != null) {
                    runOnUiThread {
                        Toast.makeText(
                            this@CadastroUsuarioActivity,
                            "Email já cadastrado no sistema",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val usuario = Usuario(
                        nome = nome,
                        email = email,
                        senha = senha,
                        estado = estado,
                        cidade = cidade,
                        endereco = endereco,
                        telefone = telefone
                    )

                    db.usuarioDao().salvar(usuario)

                    runOnUiThread {
                        Toast.makeText(
                            this@CadastroUsuarioActivity,
                            "Usuário cadastrado com sucesso",
                            Toast.LENGTH_SHORT
                        ).show()
                        limparCampos()

                        val intent = Intent(this@CadastroUsuarioActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@CadastroUsuarioActivity,
                        "Erro ao salvar: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun limparCampos() {
        binding.etNome.setText("")
        binding.etEmail.setText("")
        binding.etSenha.setText("")
        binding.etCidade.setText("")
        binding.spinnerEstado.setSelection(0)
        binding.etConfirmarSenha.setText("")
        binding.etEndereco.setText("")
        binding.etTelefone.setText("")
    }
}