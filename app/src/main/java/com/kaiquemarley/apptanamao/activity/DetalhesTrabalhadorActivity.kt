package com.kaiquemarley.apptanamao.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kaiquemarley.apptanamao.R
import com.kaiquemarley.apptanamao.dao.AppDataBase
import com.kaiquemarley.apptanamao.databinding.ActivityDetalhesTrabalhadorBinding
import com.kaiquemarley.apptanamao.extensions.ImagemURI
import com.kaiquemarley.apptanamao.extensions.NotificationHelper
import com.kaiquemarley.apptanamao.model.Disponibilidade
import com.kaiquemarley.apptanamao.model.Trabalhador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetalhesTrabalhadorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalhesTrabalhadorBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesTrabalhadorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val trabalhador = intent.getParcelableExtra<Trabalhador>("trabalhador")
        val emailAutenticado = intent.getStringExtra("EMAIL") ?: ""

        val disponibilidadeDao = AppDataBase.instancia(this).disponibilidadeDao()
        val usuarioDao = AppDataBase.instancia(this).usuarioDao()

        trabalhador?.let { trab ->
            binding.textNome.text = trab.nome
            binding.textEmail.text = trab.email
            binding.textCargo.text = trab.cargo
            binding.textCidade.text = trab.cidade.joinToString(", ")
            binding.textEstado.text = trab.estado
            binding.textEndereco.text = trab.endereco
            binding.textDescricao.text = trab.descricao ?: ""
            binding.textIdade.text = "${trab.idadeNumerica()} anos"

            val uriImagem = trab.foto?.let { nomeFoto ->
                ImagemURI.getImagemUriPeloNome(this, nomeFoto)
            }

            if (uriImagem != null) {
                binding.imageViewFoto.setImageURI(uriImagem)
                binding.imageViewFoto.setOnClickListener {
                    mostrarImagemExpandida(uriImagem)
                }
            } else {
                binding.imageViewFoto.setImageResource(R.drawable.imagem_padrao)
            }

            binding.layoutDatas.removeAllViews()

            lifecycleScope.launch {
                val usuario = withContext(Dispatchers.IO) {
                    usuarioDao.buscaUsuarioPorEmail(emailAutenticado)
                }

                val disponibilidades = disponibilidadeDao.getDisponibilidadesDoTrabalhador(trab.id)

                disponibilidades.forEach { disponibilidade ->
                    val textoBotao = when {
                        disponibilidade.reservada && disponibilidade.usuarioId == usuario?.id -> {
                            "${disponibilidade.data} às ${disponibilidade.hora} (Cancelar)"
                        }
                        disponibilidade.reservada -> {
                            "${disponibilidade.data} às ${disponibilidade.hora} (Reservado)"
                        }
                        else -> {
                            "${disponibilidade.data} às ${disponibilidade.hora}"
                        }
                    }

                    val botao = Button(this@DetalhesTrabalhadorActivity).apply {
                        text = textoBotao
                        setBackgroundColor(
                            when {
                                disponibilidade.reservada && disponibilidade.usuarioId == usuario?.id ->
                                    android.graphics.Color.parseColor("#FF5722")
                                disponibilidade.reservada ->
                                    android.graphics.Color.GRAY
                                else ->
                                    android.graphics.Color.parseColor("#FF6200EE")
                            }
                        )
                        setTextColor(android.graphics.Color.WHITE)
                        isEnabled = !disponibilidade.reservada || disponibilidade.usuarioId == usuario?.id

                        setOnClickListener {
                            if (disponibilidade.reservada && disponibilidade.usuarioId == usuario?.id) {
                                AlertDialog.Builder(this@DetalhesTrabalhadorActivity)
                                    .setTitle("Cancelar reserva")
                                    .setMessage("Deseja cancelar a reserva da data: ${disponibilidade.data} às ${disponibilidade.hora}?")
                                    .setPositiveButton("Sim") { _, _ ->
                                        cancelarReserva(disponibilidade, trab)
                                    }
                                    .setNegativeButton("Não", null)
                                    .show()
                            } else {
                                AlertDialog.Builder(this@DetalhesTrabalhadorActivity)
                                    .setTitle("Confirmar reserva")
                                    .setMessage("Deseja confirmar a data: ${disponibilidade.data} às ${disponibilidade.hora}?")
                                    .setPositiveButton("Sim") { _, _ ->
                                        reservarDisponibilidade(disponibilidade, emailAutenticado, trab)
                                    }
                                    .setNegativeButton("Não", null)
                                    .show()
                            }
                        }
                    }

                    binding.layoutDatas.addView(botao)
                }
            }
        }
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

    private fun reservarDisponibilidade(
        disponibilidade: Disponibilidade,
        emailUsuario: String,
        trabalhador: Trabalhador
    ) {
        val disponibilidadeDao = AppDataBase.instancia(this).disponibilidadeDao()
        val usuarioDao = AppDataBase.instancia(this).usuarioDao()

        lifecycleScope.launch {
            val usuario = withContext(Dispatchers.IO) {
                usuarioDao.buscaUsuarioPorEmail(emailUsuario)
            }

            if (usuario != null) {
                withContext(Dispatchers.IO) {
                    disponibilidadeDao.atualizarReserva(
                        id = disponibilidade.id,
                        reservada = true,
                        usuarioId = usuario.id
                    )
                }

                NotificationHelper.notificarReservaConfirmada(
                    this@DetalhesTrabalhadorActivity,
                    trabalhador.id,
                    "Usuário $emailUsuario reservou a data ${disponibilidade.data} às ${disponibilidade.hora}"
                )

                val intent = Intent(this@DetalhesTrabalhadorActivity, ContatoTrabalhadorActivity::class.java)
                intent.putExtra("trabalhador", trabalhador)
                startActivity(intent)
                finish()  // Opcional: fecha esta tela

            } else {
                AlertDialog.Builder(this@DetalhesTrabalhadorActivity)
                    .setTitle("Erro")
                    .setMessage("Usuário não encontrado para o email: $emailUsuario")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    private fun cancelarReserva(
        disponibilidade: Disponibilidade,
        trabalhador: Trabalhador
    ) {
        val disponibilidadeDao = AppDataBase.instancia(this).disponibilidadeDao()

        val emailUsuario = intent.getStringExtra("EMAIL") ?: "email não informado"

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                disponibilidadeDao.atualizarReserva(
                    id = disponibilidade.id,
                    reservada = false,
                    usuarioId = null
                )
            }

            runOnUiThread {
                AlertDialog.Builder(this@DetalhesTrabalhadorActivity)
                    .setTitle("Cancelado")
                    .setMessage("Reserva cancelada com sucesso!")
                    .setPositiveButton("OK") { _, _ ->
                        recarregarTela(trabalhador, emailUsuario)
                    }
                    .show()

                val mensagem = "Reserva cancelada em ${disponibilidade.data} às ${disponibilidade.hora} pelo $emailUsuario"
                NotificationHelper.notificarTrabalhadorReservaCancelada(
                    context = this@DetalhesTrabalhadorActivity,
                    trabalhadorId = trabalhador.id,
                    mensagem = mensagem
                )
            }
        }
    }

    private fun recarregarTela(trabalhador: Trabalhador, emailUsuario: String) {
        val intent = intent
        intent.putExtra("trabalhador", trabalhador)
        intent.putExtra("EMAIL", emailUsuario)
        finish()
        startActivity(intent)
    }
}