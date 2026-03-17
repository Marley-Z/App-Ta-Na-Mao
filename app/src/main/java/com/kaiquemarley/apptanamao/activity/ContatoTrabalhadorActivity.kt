package com.kaiquemarley.apptanamao.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kaiquemarley.apptanamao.databinding.ActivityContatoTrabalhadorBinding
import com.kaiquemarley.apptanamao.model.Trabalhador

class ContatoTrabalhadorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContatoTrabalhadorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContatoTrabalhadorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val trabalhador = intent.getParcelableExtra<Trabalhador>("trabalhador")

        trabalhador?.let {
            binding.textTelefoneContato.text = it.telefone
            binding.textEmailContato.text = it.email
        }
    }
}