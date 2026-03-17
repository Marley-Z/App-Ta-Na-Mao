package com.kaiquemarley.apptanamao.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.kaiquemarley.apptanamao.databinding.ActivityCadastroTrabalhadorBinding
import com.kaiquemarley.apptanamao.model.Trabalhador
import com.kaiquemarley.apptanamao.dao.AppDataBase
import com.kaiquemarley.apptanamao.extensions.DataUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CadastroTrabalhadorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroTrabalhadorBinding
    private lateinit var db: AppDataBase

    // Listas definidas uma única vez, fora do onCreate
    private val cargos = listOf(
        "Selecione seu serviço",
        "Chaveiro",
        "Dedetizador",
        "Eletricista",
        "Encanador",
        "Gesseiro",
        "Instalador de Antenas",
        "Instalador de Ar Condicionado",
        "Jardineiro",
        "Marceneiro",
        "Mecânico",
        "Montador de Móveis",
        "Motoboy",
        "Pedreiro",
        "Refrigerista",
        "Serralheiro",
        "Soldador",
        "Vidraceiro"

    )

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



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroTrabalhadorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDataBase.instancia(this)

        // Adapter para cargos
        val cargoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cargos)
        cargoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCargo.adapter = cargoAdapter

        // Validação de senha enquanto digita
        binding.etSenha.doOnTextChanged { text, _, _, _ ->
            val senha = text.toString()
            binding.tvSenhaAviso.visibility = if (senha.length in 1..5) View.VISIBLE else View.GONE
        }

// Validação de CPF enquanto digita
        binding.etCpf.doOnTextChanged { text, _, _, _ ->
            val cpf = text.toString()
            binding.tvCpfAviso.visibility = if (cpf.length in 1..13) View.VISIBLE else View.GONE
        }

// Validação de Telefone enquanto digita
        binding.etTelefone.doOnTextChanged { text, _, _, _ ->
            val telefone = text.toString()
            binding.tvTelefoneAviso.visibility =
                if (telefone.length in 1..14) View.VISIBLE else View.GONE
        }



        // Máscara para telefone: (00) 00000-0000
        aplicarMascara(binding.etTelefone, "(##) #####-####")

// Máscara para CPF: 000.000.000-00
        aplicarMascara(binding.etCpf,
            "###.###.###-##")
//Mascara pra data de nasicmento
        aplicarMascara(binding.etDataNascimento, "##/##/####")


        // Adapter para estados
        val estadoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, estados)
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerEstado.adapter = estadoAdapter

        binding.btnSalvar.setOnClickListener {
            salvarTrabalhador()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun salvarTrabalhador() {
        val nome = binding.etNome.text.toString().trim()
        val cpf = binding.etCpf.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val senha = binding.etSenha.text.toString().trim()
        val cargo = binding.spinnerCargo.selectedItem.toString()
        val estado = binding.spinnerEstado.selectedItem.toString()
        val cidade = binding.etCidade.text.toString().split(",").map { it.trim() }
        val dataNascimento = binding.etDataNascimento.text.toString().trim()
        val endereco = binding.etEndereco.text.toString().trim()
        val telefone = binding.etTelefone.text.toString().trim()
        val confirmarSenha = binding.etConfirmarSenha.text.toString().trim()

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() ||
            cargo.isEmpty() || dataNascimento.isEmpty() || endereco.isEmpty()
            || telefone.isEmpty() || cidade.isEmpty() || cpf.isEmpty()
        ) {
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

        if (senha != confirmarSenha) {
            Toast.makeText(this, "As senhas não coincidem.", Toast.LENGTH_SHORT).show()
            return
        }

        val idade = try {
            DataUtil.calcularIdade(dataNascimento)
        } catch (e: Exception) {
            Toast.makeText(this, "Data de nascimento inválida.", Toast.LENGTH_SHORT).show()
            return
        }

        if (idade <= 0) {
            Toast.makeText(this, "Idade inválida.", Toast.LENGTH_SHORT).show()
            return
        }


        if (cpf.length < 14) {
            Toast.makeText(this, "CPF inválido. Deve ter 11 dígitos.", Toast.LENGTH_SHORT).show()
            return
        }

        if (telefone.length < 15) {
            Toast.makeText(this, "Telefone inválido. Deve ter o formato (00) 00000-0000.", Toast.LENGTH_SHORT).show()
            return
        }

        if (cargo == "Selecione seu serviço") {
            Toast.makeText(this, "Por favor, selecione um serviço válido.", Toast.LENGTH_SHORT).show()
            return
        }

        if (estado == "Selecione seu estado") {
            Toast.makeText(this, "Por favor, selecione um estado.", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val usuarioExistente = db.usuarioDao().getUsuarioPorEmail(email)
                val trabalhadorExistente = db.trabalhadorDao().getTrabalhadorPorEmail(email)

                if (usuarioExistente != null || trabalhadorExistente != null) {
                    runOnUiThread {
                        Toast.makeText(
                            this@CadastroTrabalhadorActivity,
                            "Email já cadastrado no sistema",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val trabalhador = Trabalhador(
                        nome = nome,
                        cpf = cpf,
                        email = email,
                        senha = senha,
                        dataNascimento = dataNascimento,
                        cargo = cargo,
                        estado = estado,
                        cidade = cidade,
                        endereco = endereco,
                        telefone = telefone
                    )

                    db.trabalhadorDao().salvar(trabalhador)

                    runOnUiThread {
                        Toast.makeText(
                            this@CadastroTrabalhadorActivity,
                            "Trabalhador cadastrado com sucesso",
                            Toast.LENGTH_SHORT
                        ).show()
                        limparCampos()

                        val intent = Intent(this@CadastroTrabalhadorActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()  // Mostra a stack trace no Logcat

                Log.e("CadastroTrabalhador", "Erro ao cadastrar trabalhador", e)  // Log com tag

                runOnUiThread {
                    Toast.makeText(
                        this@CadastroTrabalhadorActivity,
                        "Erro ao cadastrar trabalhador: ${e.message}",
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
        binding.etConfirmarSenha.setText("")
        binding.etCpf.setText("")
        binding.etCidade.setText("")
        binding.spinnerCargo.setSelection(0)
        binding.spinnerEstado.setSelection(0)
        binding.etDataNascimento.setText("")
        binding.etEndereco.setText("")
        binding.etTelefone.setText("")
    }
}