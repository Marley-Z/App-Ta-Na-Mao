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
import com.kaiquemarley.apptanamao.activity.LoginActivity
import com.kaiquemarley.apptanamao.dao.AppDataBase
import com.kaiquemarley.apptanamao.databinding.ActivityPerfilUsuarioBinding
import com.kaiquemarley.apptanamao.extensions.NomeImagem
import com.kaiquemarley.apptanamao.model.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class PerfilUsuarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilUsuarioBinding
    private lateinit var usuario: Usuario
    private val usuarioDao by lazy { AppDataBase.instancia(this).usuarioDao() }
    private var uriFotoNova: Uri? = null

    private val estados = listOf(
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

    private val selecionaFoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { fotoSelecionada ->
            AlertDialog.Builder(this)
                .setTitle("Confirmar alteração")
                .setMessage("Tem certeza que deseja alterar a foto de perfil?")
                .setPositiveButton("Sim") { _, _ ->
                    uriFotoNova = fotoSelecionada
                    binding.imageViewPerfil.setImageURI(fotoSelecionada)

                    // Ao clicar, mostrar a imagem expandida
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
        binding = ActivityPerfilUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Máscara para telefone: (00) 00000-0000
        aplicarMascara(binding.editTextTelefone, "(##) #####-####")

        val emailLogado = intent.getStringExtra("EMAIL") ?: return

        val estadoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, estados)
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerEstado.adapter = estadoAdapter

        CoroutineScope(Dispatchers.IO).launch {
            usuario = usuarioDao.buscaUsuarioPorEmail(emailLogado) ?: return@launch
            runOnUiThread { preencheCampos() }
        }

        binding.btnAlterarFoto.setOnClickListener { selecionaFoto.launch("image/*") }
        binding.btnExcluirFoto.setOnClickListener { confirmaExclusaoFoto() }
        binding.btnSalvar.setOnClickListener { confirmaSalvarAlteracoes() }
        binding.btnDeletar.setOnClickListener { confirmaExclusao() }
        binding.btnAlterarSenha.setOnClickListener { verificaSenhaAtual() }
    }

    private fun preencheCampos() {
        binding.editTextNome.setText(usuario.nome)
        binding.editTextEmail.setText(usuario.email)
        binding.editTextEndereco.setText(usuario.endereco)
        binding.editTextTelefone.setText(usuario.telefone)
        binding.editTextCidade.setText(usuario.cidade)

        val posicaoEstado = estados.indexOf(usuario.estado)
        if (posicaoEstado >= 0) {
            binding.spinnerEstado.setSelection(posicaoEstado)
        } else {
            binding.spinnerEstado.setSelection(0)  // Seleciona a primeira opção se não encontrado
        }

        usuario.foto?.let { nomeFoto ->
            val caminho = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/AppTanamao/$nomeFoto"
            val file = File(caminho)
            if (file.exists()) {
                binding.imageViewPerfil.setImageURI(Uri.fromFile(file))
            } else {
                binding.imageViewPerfil.setImageResource(R.drawable.imagem_padrao)
            }
        } ?: binding.imageViewPerfil.setImageResource(R.drawable.imagem_padrao)

        binding.imageViewPerfil.setOnClickListener {
            val uriExpandida = if (usuario.foto != null) {
                val caminho = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/AppTanamao/${usuario.foto}"
                val file = File(caminho)
                if (file.exists()) Uri.fromFile(file) else null
            } else {
                null
            }

            uriExpandida?.let { mostrarImagemExpandida(it) }
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
            if (senhaDigitada == usuario.senha) {
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
            usuario = usuario.copy(senha = novaSenha)
            usuarioDao.atualiza(usuario)
            runOnUiThread {
                Toast.makeText(this@PerfilUsuarioActivity, "Senha atualizada com sucesso", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmaExclusaoFoto() {
        AlertDialog.Builder(this)
            .setTitle("Excluir Foto")
            .setMessage("Tem certeza que deseja excluir a foto de perfil e voltar para a imagem padrão?")
            .setPositiveButton("Sim") { _, _ -> excluiFoto() }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun excluiFoto() {
        CoroutineScope(Dispatchers.IO).launch {
            usuario.foto?.let { deletarImagem(it) }
            usuario = usuario.copy(foto = null)
            usuarioDao.atualiza(usuario)
            runOnUiThread {
                preencheCampos()
                Toast.makeText(this@PerfilUsuarioActivity, "Foto excluída com sucesso", Toast.LENGTH_SHORT).show()
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
        val cidade = binding.editTextCidade.text.toString().trim()
        val estadoSelecionado = binding.spinnerEstado.selectedItem.toString()
        val telefone = binding.editTextTelefone.text.toString().trim()

        if (nome.isEmpty() || email.isEmpty() || endereco.isEmpty() || telefone.isEmpty() || cidade.isEmpty()) {
            Toast.makeText(this, "Todos os campos devem ser preenchidos", Toast.LENGTH_SHORT).show()
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

        CoroutineScope(Dispatchers.IO).launch {
            val outroUsuario = usuarioDao.getUsuarioPorEmail(email)
            val outroTrabalhador = AppDataBase.instancia(this@PerfilUsuarioActivity).trabalhadorDao().getTrabalhadorPorEmail(email)

            if ((outroUsuario != null && outroUsuario.id != usuario.id) || outroTrabalhador != null) {
                runOnUiThread {
                    Toast.makeText(
                        this@PerfilUsuarioActivity,
                        "Esse email já está cadastrado em outro usuário ou trabalhador",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            }

            val nomeFoto = uriFotoNova?.let { salvarImagemPublica(it, usuario.email) }
            val fotoFinal = nomeFoto ?: usuario.foto

            val usuarioAtualizado = usuario.copy(
                nome = nome,
                email = email,
                endereco = endereco,
                telefone = telefone,
                cidade = cidade,
                estado = estadoSelecionado,
                foto = fotoFinal
            )

            usuarioDao.atualiza(usuarioAtualizado)
            usuario = usuarioAtualizado

            runOnUiThread {
                preencheCampos()
                Toast.makeText(this@PerfilUsuarioActivity, "Alterações salvas", Toast.LENGTH_SHORT).show()
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
            uriDestino?.let {
                resolver.openInputStream(uri)?.use { input ->
                    resolver.openOutputStream(it)?.use { output ->
                        input.copyTo(output)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)
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
            usuario.foto?.let { deletarImagem(it) }
            usuarioDao.deleta(usuario)
            runOnUiThread {
                Toast.makeText(this@PerfilUsuarioActivity, "Conta excluída com sucesso", Toast.LENGTH_SHORT).show()

                // Ir para tela de login
                val intent = Intent(this@PerfilUsuarioActivity, LoginActivity::class.java)
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

        imageViewExpandida.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            (resources.displayMetrics.heightPixels * 0.9).toInt()
        )

        dialog.show()
    }
}