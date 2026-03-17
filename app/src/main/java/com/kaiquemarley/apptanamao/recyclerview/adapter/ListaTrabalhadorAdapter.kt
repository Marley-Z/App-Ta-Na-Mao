package com.kaiquemarley.apptanamao.recyclerview.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaiquemarley.apptanamao.databinding.TrabalhadorItemBinding
import com.kaiquemarley.apptanamao.extensions.ImagemURI
import com.kaiquemarley.apptanamao.model.Trabalhador
import com.kaiquemarley.apptanamao.R

class ListaTrabalhadorAdapter(
    private val context: Context,
    private var lista: List<Trabalhador>,
    private val quandoClicaNoItem: (Trabalhador) -> Unit = {}
) : RecyclerView.Adapter<ListaTrabalhadorAdapter.TrabalhadorViewHolder>() {

    inner class TrabalhadorViewHolder(private val binding: TrabalhadorItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var trabalhador: Trabalhador

        init {
            itemView.setOnClickListener {
                if (::trabalhador.isInitialized) {
                    quandoClicaNoItem(trabalhador)
                }
            }
        }

        fun vincula(trabalhador: Trabalhador) {
            this.trabalhador = trabalhador


            binding.textNome.text = trabalhador.nome
            binding.textCargo.text = trabalhador.cargo
            binding.textCidade.text = trabalhador.cidade.joinToString(", ")
            binding.textEstado.text = trabalhador.estado

            binding.textDescricao.text = trabalhador.descricao ?: ""

            val uriImagem = trabalhador.foto?.let {
                ImagemURI.getImagemUriPeloNome(context, it)
            }

            if (uriImagem != null) {
                binding.imageViewFoto.setImageURI(uriImagem)
            } else {
                binding.imageViewFoto.setImageResource(R.drawable.imagem_padrao)
            }
        }
    }

    fun atualiza(novaLista: List<Trabalhador>) {
        lista = novaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrabalhadorViewHolder {
        val binding = TrabalhadorItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return TrabalhadorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrabalhadorViewHolder, position: Int) {
        val trabalhador = lista[position]
        holder.vincula(trabalhador)
    }

    override fun getItemCount(): Int = lista.size
}
