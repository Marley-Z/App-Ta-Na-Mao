package com.kaiquemarley.apptanamao.recyclerview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.kaiquemarley.apptanamao.R

class CargoAdapter(
    private val cargos: List<String>,
    private val onCargoClick: (String) -> Unit
) : RecyclerView.Adapter<CargoAdapter.CargoViewHolder>() {

    private var cargoSelecionado: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CargoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cargo, parent, false)
        return CargoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CargoViewHolder, position: Int) {
        val cargo = cargos[position]
        holder.bind(cargo)
    }

    override fun getItemCount(): Int = cargos.size

    inner class CargoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val button: Button = view.findViewById(R.id.btnCargo)

        fun bind(cargo: String) {
            button.text = cargo

            val context = button.context
            val drawableRes = if (cargo == cargoSelecionado) {
                R.drawable.botao_cargo_selecionado
            } else {
                R.drawable.botao_cargo_normal
            }
            button.background = context.getDrawable(drawableRes)
            button.backgroundTintList = null  // <- ESSENCIAL!

            button.setOnClickListener {
                cargoSelecionado = cargo
                notifyDataSetChanged()  // Atualiza toda a lista
                onCargoClick(cargo)
            }
        }

    }
}
