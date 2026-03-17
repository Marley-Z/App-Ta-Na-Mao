package com.kaiquemarley.apptanamao.activity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaiquemarley.apptanamao.dao.AppDataBase
import com.kaiquemarley.apptanamao.databinding.ActivityAdicionarDataBinding
import com.kaiquemarley.apptanamao.extensions.NotificationHelper
import com.kaiquemarley.apptanamao.model.Disponibilidade
import com.kaiquemarley.apptanamao.model.Usuario
import com.kaiquemarley.apptanamao.recyclerview.adapter.DisponibilidadeAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AdicionarDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdicionarDataBinding
    private lateinit var adapter: DisponibilidadeAdapter
    private lateinit var db: AppDataBase

    private val listaDisponibilidades = mutableListOf<Disponibilidade>()
    private var trabalhadorId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdicionarDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDataBase.instancia(this)

        trabalhadorId = intent.getLongExtra("TRABALHADOR_ID", 0L)
        Log.d("AdicionarDataActivity", "Recebido trabalhadorId: $trabalhadorId")

        if (trabalhadorId == 0L) {
            Toast.makeText(this, "Erro: trabalhadorId não recebido!", Toast.LENGTH_LONG).show()
            Log.e("AdicionarDataActivity", "trabalhadorId NÃO recebido ou está 0")
            finish()
            return
        }

        adapter = DisponibilidadeAdapter(listaDisponibilidades) { usuarioId ->
            db.usuarioDao().buscaUsuarioPorId(usuarioId)
        }

        binding.rvDatas.layoutManager = LinearLayoutManager(this)
        binding.rvDatas.adapter = adapter

        binding.btnAdicionarData.setOnClickListener {
            mostrarDatePicker()
        }

        adapter.setOnItemLongClickListener { disponibilidade ->
            mostrarDialogConfirmarExclusao(disponibilidade)
        }

        carregarDisponibilidades()
    }

    private fun mostrarDatePicker() {
        val calendario = Calendar.getInstance()
        val ano = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, anoEscolhido, mesEscolhido, diaEscolhido ->
            val data = String.format("%02d/%02d/%04d", diaEscolhido, mesEscolhido + 1, anoEscolhido)
            mostrarTimePicker(data)
        }, ano, mes, dia).show()
    }

    private fun mostrarTimePicker(data: String) {
        val calendario = Calendar.getInstance()
        val hora = calendario.get(Calendar.HOUR_OF_DAY)
        val minuto = calendario.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, horaEscolhida, minutoEscolhido ->
            val horaFormatada = String.format("%02d:%02d", horaEscolhida, minutoEscolhido)

            val disponibilidade = Disponibilidade(
                trabalhadorId = trabalhadorId,
                data = data,
                hora = horaFormatada
            )

            Log.d("AdicionarDataActivity", "Salvando disponibilidade: $disponibilidade")

            salvarDisponibilidade(disponibilidade)
        }, hora, minuto, true).show()
    }

    private fun salvarDisponibilidade(disponibilidade: Disponibilidade) {
        CoroutineScope(Dispatchers.IO).launch {
            db.disponibilidadeDao().inserir(disponibilidade)
            runOnUiThread {
                listaDisponibilidades.add(disponibilidade)
                adapter.notifyItemInserted(listaDisponibilidades.size - 1)
                Toast.makeText(
                    this@AdicionarDataActivity,
                    "Disponibilidade adicionada!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun carregarDisponibilidades() {
        CoroutineScope(Dispatchers.IO).launch {
            val lista = db.disponibilidadeDao().getDisponibilidadesDoTrabalhador(trabalhadorId)
            runOnUiThread {
                listaDisponibilidades.clear()
                listaDisponibilidades.addAll(lista)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun mostrarDialogConfirmarExclusao(disponibilidade: Disponibilidade) {
        CoroutineScope(Dispatchers.IO).launch {
            val usuarioAssociado = disponibilidade.usuarioId?.let { usuarioId ->
                db.usuarioDao().buscaUsuarioPorId(usuarioId)
            }

            runOnUiThread {
                val mensagem = if (usuarioAssociado != null) {
                    "Tem certeza que deseja excluir a data ${disponibilidade.data} às ${disponibilidade.hora}?\n\nHá um usuário associado (${usuarioAssociado.nome}). Deseja mesmo excluir?"
                } else {
                    "Tem certeza que deseja excluir a data ${disponibilidade.data} às ${disponibilidade.hora}?"
                }

                AlertDialog.Builder(this@AdicionarDataActivity)
                    .setTitle("Excluir disponibilidade")
                    .setMessage(mensagem)
                    .setPositiveButton("Sim") { _, _ ->
                        excluirDisponibilidade(disponibilidade, usuarioAssociado)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    private fun excluirDisponibilidade(disponibilidade: Disponibilidade, usuario: Usuario?) {
        CoroutineScope(Dispatchers.IO).launch {
            db.disponibilidadeDao().deleta(disponibilidade)
            runOnUiThread {
                val index = listaDisponibilidades.indexOfFirst { it.id == disponibilidade.id }
                if (index != -1) {
                    listaDisponibilidades.removeAt(index)
                    adapter.notifyItemRemoved(index)
                }
                Toast.makeText(
                    this@AdicionarDataActivity,
                    "Disponibilidade excluída!",
                    Toast.LENGTH_SHORT
                ).show()

                usuario?.let {
                    notificarUsuario(it, disponibilidade)
                }
            }
        }
    }

    private fun notificarUsuario(usuario: Usuario, disponibilidade: Disponibilidade) {
        val emailTrabalhador = intent.getStringExtra("EMAIL") ?: "email não informado"

        Log.d(
            "Notificação",
            "Usuário ${usuario.nome} notificado: A disponibilidade em ${disponibilidade.data} às ${disponibilidade.hora} foi cancelada."
        )

        Toast.makeText(
            this,
            "Usuário ${usuario.nome} será notificado sobre a exclusão.",
            Toast.LENGTH_LONG
        ).show()


        NotificationHelper.notificarReservaCancelada(
            context = this,
            trabalhadorId = trabalhadorId,
            mensagem = "Sua reserva em ${disponibilidade.data} às ${disponibilidade.hora} foi cancelada pelo trabalhador"
        )
    }
}