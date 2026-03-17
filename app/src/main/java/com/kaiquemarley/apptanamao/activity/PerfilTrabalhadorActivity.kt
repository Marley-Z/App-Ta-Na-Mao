package com.kaiquemarley.apptanamao.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.kaiquemarley.apptanamao.R
import com.kaiquemarley.apptanamao.dao.AppDataBase
import com.kaiquemarley.apptanamao.databinding.ActivityPerfilTrabalhadorBinding
import com.kaiquemarley.apptanamao.extensions.NomeImagem
import com.kaiquemarley.apptanamao.model.Trabalhador
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class PerfilTrabalhadorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilTrabalhadorBinding
    private lateinit var trabalhador: Trabalhador
    private val trabalhadorDao by lazy { AppDataBase.instancia(this).trabalhadorDao() }
    private val cargos = listOf(
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
        "AC", "AL", "AM", "AP", "BA", "CE", "DF", "ES", "GO",
        "MA", "MG", "MS", "MT", "PA", "PB", "PE", "PI", "PR",
        "RJ", "RN", "RO", "RR", "RS", "SC", "SE", "SP", "TO"
    )

    private fun aplicarMascara(editText: android.widget.EditText, mask: String) {
        var isUpdating = false
        var oldText = ""

        editText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

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

    private var uriFotoNova: Uri? = null

    private val selecionaFoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { fotoSelecionada ->
                AlertDialog.Builder(this)
                    .setTitle("Confirmar alteração")
                    .setMessage("Tem certeza que deseja alterar a foto de perfil?")
                    .setPositiveButton("Sim") { _, _ ->
                        uriFotoNova = fotoSelecionada
                        binding.imageViewPerfil.setImageURI(fotoSelecionada)
                        binding.imageViewPerfil.setOnClickListener {
                            mostrarImagemExpandida(fotoSelecionada)
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilTrabalhadorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cargos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCargo.adapter = adapter

        val estadoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, estados)
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerEstado.adapter = estadoAdapter

        aplicarMascara(binding.editTextTelefone, "(##) #####-####")

        val emailLogado = intent.getStringExtra("EMAIL") ?: return

        CoroutineScope(Dispatchers.IO).launch {
            trabalhador = trabalhadorDao.buscaTrabalhadorPorEmail(emailLogado) ?: return@launch
            runOnUiThread {
                preencheCampos()

                // Configura o listener APENAS depois que o trabalhador foi carregado
                binding.btnDatas.setOnClickListener {
                    val intent = Intent(this@PerfilTrabalhadorActivity, AdicionarDataActivity::class.java)
                    intent.putExtra("TRABALHADOR_ID", trabalhador.id)
                    intent.putExtra("EMAIL", trabalhador.email)
                    startActivity(intent)
                }
            }
        }

        binding.btnAlterarFoto.setOnClickListener { selecionaFoto.launch("image/*") }
        binding.btnExcluirFoto.setOnClickListener { confirmaExclusaoFoto() }
        binding.btnSalvar.setOnClickListener { confirmaSalvarAlteracoes() }
        binding.btnDeletar.setOnClickListener { confirmaExclusao() }
        binding.btnAlterarSenha.setOnClickListener { verificaSenhaAtual() }


    }





    private fun preencheCampos() {
        binding.editTextNome.setText(trabalhador.nome)
        binding.editTextEmail.setText(trabalhador.email)
        binding.editTextEndereco.setText(trabalhador.endereco)
        binding.editTextCidade.setText(trabalhador.cidade.joinToString(", "))
        binding.editTextTelefone.setText(trabalhador.telefone)
        binding.editTextDescricao.setText(trabalhador.descricao)

        val posicaoCargo = cargos.indexOf(trabalhador.cargo)
        if (posicaoCargo >= 0) binding.spinnerCargo.setSelection(posicaoCargo)

        val posicaoEstado = estados.indexOf(trabalhador.estado)
        if (posicaoEstado >= 0) binding.spinnerEstado.setSelection(posicaoEstado)

        trabalhador.foto?.let { nomeFoto ->
            val caminho = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/AppTanamao/$nomeFoto"
            val file = File(caminho)
            if (file.exists()) {
                binding.imageViewPerfil.setImageURI(Uri.fromFile(file))
            } else {
                binding.imageViewPerfil.setImageResource(R.drawable.imagem_padrao)
            }
        } ?: binding.imageViewPerfil.setImageResource(R.drawable.imagem_padrao)

        binding.imageViewPerfil.setOnClickListener {
            trabalhador.foto?.let { nomeFoto ->
                val caminho = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/AppTanamao/$nomeFoto"
                val file = File(caminho)
                if (file.exists()) {
                    mostrarImagemExpandida(Uri.fromFile(file))
                }
            }
        }
    }

    private fun verificaSenhaAtual() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirme sua senha atual")
        val input = android.widget.EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)

        builder.setPositiveButton("Confirmar") { dialog, _ ->
            val senhaDigitada = input.text.toString()
            if (senhaDigitada == trabalhador.senha) {
                mostrarDialogNovaSenha()
            } else {
                Toast.makeText(this, "Senha incorreta!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun mostrarDialogNovaSenha() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Digite a nova senha")
        val input = android.widget.EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)

        builder.setPositiveButton("Confirmar") { dialog, _ ->
            val novaSenha = input.text.toString()
            if (novaSenha.isEmpty()) {
                Toast.makeText(this, "A nova senha não pode ser vazia", Toast.LENGTH_SHORT).show()
            } else if (novaSenha.length < 6) {
                Toast.makeText(this, "A nova senha deve ter no mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
            } else {
                atualizaSenha(novaSenha)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun atualizaSenha(novaSenha: String) {
        CoroutineScope(Dispatchers.IO).launch {
            trabalhador = trabalhador.copy(senha = novaSenha)
            trabalhadorDao.atualiza(trabalhador)
            runOnUiThread {
                Toast.makeText(this@PerfilTrabalhadorActivity, "Senha atualizada com sucesso", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmaExclusaoFoto() {
        AlertDialog.Builder(this)
            .setTitle("Excluir Foto")
            .setMessage("Tem certeza que deseja excluir a foto de perfil?")
            .setPositiveButton("Sim") { _, _ -> excluiFoto() }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun excluiFoto() {
        CoroutineScope(Dispatchers.IO).launch {
            trabalhador.foto?.let { deletarImagem(it) }
            trabalhador = trabalhador.copy(foto = null)
            trabalhadorDao.atualiza(trabalhador)
            runOnUiThread {
                preencheCampos()
                Toast.makeText(this@PerfilTrabalhadorActivity, "Foto excluída com sucesso", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmaSalvarAlteracoes() {
        AlertDialog.Builder(this)
            .setTitle("Salvar Alterações")
            .setMessage("Tem certeza que deseja salvar as alterações?")
            .setPositiveButton("Sim") { _, _ -> salvaAlteracoes() }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun salvaAlteracoes() {
        val nome = binding.editTextNome.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val endereco = binding.editTextEndereco.text.toString().trim()
        val telefone = binding.editTextTelefone.text.toString().trim()
        val cargoSelecionado = binding.spinnerCargo.selectedItem.toString()
        val estadoSelecionado = binding.spinnerEstado.selectedItem.toString()
        val cidade = binding.editTextCidade.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        val descricao = binding.editTextDescricao.text.toString().trim()

        if (nome.isEmpty() || email.isEmpty() || endereco.isEmpty() || telefone.isEmpty() || descricao.isEmpty() || cidade.isEmpty()) {
            Toast.makeText(this, "Todos os campos devem ser preenchidos", Toast.LENGTH_SHORT).show()
            return
        }

        if (telefone.length < 15) {
            Toast.makeText(
                this,
                "Telefone inválido. Deve ter o formato (00) 00000-0000.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!email.endsWith("@gmail.com")) {
            Toast.makeText(this, "O email deve terminar com @gmail.com", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val outroTrabalhador = trabalhadorDao.buscaTrabalhadorPorEmail(email)
            val outroUsuario = AppDataBase.instancia(this@PerfilTrabalhadorActivity).usuarioDao()
                .buscaUsuarioPorEmail(email)

            if ((outroTrabalhador != null && outroTrabalhador.id != trabalhador.id) || (outroUsuario != null)) {
                runOnUiThread {
                    Toast.makeText(
                        this@PerfilTrabalhadorActivity,
                        "Esse email já está cadastrado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch


            }

            val nomeFoto = uriFotoNova?.let { salvarImagemPublica(it, trabalhador.email) }
            val fotoFinal = nomeFoto ?: trabalhador.foto

            val trabalhadorAtualizado = trabalhador.copy(
                nome = nome,
                email = email,
                endereco = endereco,
                telefone = telefone,
                cargo = cargoSelecionado,
                estado = estadoSelecionado,
                cidade = cidade,
                descricao = descricao,
                foto = fotoFinal
            )
            trabalhadorDao.atualiza(trabalhadorAtualizado)
            trabalhador = trabalhadorAtualizado

            runOnUiThread {
                preencheCampos()
                Toast.makeText(
                    this@PerfilTrabalhadorActivity,
                    "Alterações salvas",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }
    private fun salvarImagemPublica(uri: Uri, email: String): String? {
        val resolver = contentResolver
        val nomeArquivo = NomeImagem.gerarNomeArquivo(email)
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, nomeArquivo)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/AppTanamao")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            } else {
                val imagensDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AppTanamao")
                if (!imagensDir.exists()) imagensDir.mkdirs()
                put(MediaStore.MediaColumns.DATA, File(imagensDir, nomeArquivo).absolutePath)
            }
        }

        val uriDestino = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        try {
            if (uriDestino != null) {
                resolver.openInputStream(uri)?.use { input ->
                    resolver.openOutputStream(uriDestino)?.use { output ->
                        input.copyTo(output)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uriDestino, contentValues, null, null)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return nomeArquivo
    }

    private fun confirmaExclusao() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar exclusão")
            .setMessage("Tem certeza que deseja excluir sua conta? Esta ação não pode ser desfeita.")
            .setPositiveButton("Sim") { _, _ -> deletaConta() }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun deletaConta() {
        CoroutineScope(Dispatchers.IO).launch {
            trabalhador.foto?.let { deletarImagem(it) }
            trabalhadorDao.deleta(trabalhador)
            runOnUiThread {
                Toast.makeText(this@PerfilTrabalhadorActivity, "Conta excluída com sucesso", Toast.LENGTH_SHORT).show()

                // Ir para tela de login
                val intent = Intent(this@PerfilTrabalhadorActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

                finish()
            }
        }
    }




    private fun deletarImagem(nomeFoto: String) {
        val caminho = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/AppTanamao/$nomeFoto"
        val file = File(caminho)
        if (file.exists()) file.delete()
    }

    private fun mostrarImagemExpandida(uri: Uri) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.imagem_expandida)

        val imageViewExpandida = dialog.findViewById<ImageView>(R.id.imageViewExpandida)
        imageViewExpandida.setImageURI(uri)
        imageViewExpandida.adjustViewBounds = true
        imageViewExpandida.scaleType = ImageView.ScaleType.FIT_CENTER
        imageViewExpandida.setOnClickListener { dialog.dismiss() }

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            (resources.displayMetrics.heightPixels * 0.9).toInt()
        )
        dialog.show()
    }
}