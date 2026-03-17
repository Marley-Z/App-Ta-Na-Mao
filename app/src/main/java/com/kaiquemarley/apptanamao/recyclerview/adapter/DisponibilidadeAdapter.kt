package com.kaiquemarley.apptanamao.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaiquemarley.apptanamao.databinding.ItemDisponibilidadeBinding
import com.kaiquemarley.apptanamao.extensions.ImagemURI
import com.kaiquemarley.apptanamao.model.Disponibilidade
import com.kaiquemarley.apptanamao.model.Usuario
import com.kaiquemarley.apptanamao.R
import kotlinx.coroutines.*

class DisponibilidadeAdapter(
    private val lista: List<Disponibilidade>,
    private val buscarUsuarioPorId: suspend (Long) -> Usuario?
) : RecyclerView.Adapter<DisponibilidadeAdapter.ViewHolder>() {

    private var onItemLongClickListener: ((Disponibilidade) -> Unit)? = null

    fun setOnItemLongClickListener(listener: (Disponibilidade) -> Unit) {
        onItemLongClickListener = listener
    }

    inner class ViewHolder(val binding: ItemDisponibilidadeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemDisponibilidadeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val disponibilidade = lista[position]

        holder.binding.tvData.text = "Data: ${disponibilidade.data}"
        holder.binding.tvHora.text = "Hora: ${disponibilidade.hora}"

        val usuarioId = disponibilidade.usuarioId

        if (usuarioId != null) {
            holder.binding.tvUsuarioReservou.visibility = android.view.View.VISIBLE
            holder.binding.tvUsuarioReservou.text = "Carregando informações do usuário..."

            CoroutineScope(Dispatchers.Main).launch {
                val usuario = withContext(Dispatchers.IO) {
                    buscarUsuarioPorId(usuarioId)
                }

                if (usuario != null) {
                    holder.binding.tvUsuarioReservou.text = """
                    Nome: ${usuario.nome}
                    Estado: ${usuario.estado}
                    Cidade: ${usuario.cidade}
                    Endereço: ${usuario.endereco}
                    Telefone: ${usuario.telefone}
                """.trimIndent()
                    holder.binding.tvUsuarioReservou.setTextColor(
                        holder.itemView.context.getColor(android.R.color.white)
                    )

                    val uriImagem = usuario.foto?.let {
                        ImagemURI.getImagemUriPeloNome(holder.itemView.context, it)
                    }

                    // Sempre mostrar a ImageView
                    holder.binding.imageViewFotoUsuario.visibility = android.view.View.VISIBLE

                    if (uriImagem != null) {
                        holder.binding.imageViewFotoUsuario.setImageURI(uriImagem)
                    } else {
                        holder.binding.imageViewFotoUsuario.setImageResource(R.drawable.imagem_padrao)
                    }

                } else {
                    holder.binding.tvUsuarioReservou.text = "Usuário não encontrado"
                    holder.binding.tvUsuarioReservou.setTextColor(
                        holder.itemView.context.getColor(android.R.color.holo_red_dark)
                    )
                    // Aqui como usuário não existe, esconde a imagem
                    holder.binding.imageViewFotoUsuario.visibility = android.view.View.GONE
                    holder.binding.imageViewFotoUsuario.setImageDrawable(null)
                }
            }
        } else {
            holder.binding.tvUsuarioReservou.visibility = android.view.View.VISIBLE
            holder.binding.tvUsuarioReservou.text = "Disponível"
            holder.binding.tvUsuarioReservou.setTextColor(
                holder.itemView.context.getColor(com.kaiquemarley.apptanamao.R.color.roxo_padroao)
            )
            // Não tem usuário, esconde a imagem
            holder.binding.imageViewFotoUsuario.visibility = android.view.View.GONE
            holder.binding.imageViewFotoUsuario.setImageDrawable(null)
        }

        // Configura o long click
        holder.itemView.setOnLongClickListener {
            onItemLongClickListener?.invoke(disponibilidade)
            true
        }
    }}