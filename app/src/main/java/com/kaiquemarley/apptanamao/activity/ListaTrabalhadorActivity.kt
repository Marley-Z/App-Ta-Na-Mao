package com.kaiquemarley.apptanamao.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kaiquemarley.apptanamao.R
import com.kaiquemarley.apptanamao.dao.AppDataBase
import com.kaiquemarley.apptanamao.extensions.ImagemURI
import com.kaiquemarley.apptanamao.recyclerview.adapter.CargoAdapter
import com.kaiquemarley.apptanamao.recyclerview.adapter.ListaTrabalhadorAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListaTrabalhadorActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var imageViewPerfilUsuario: ImageView
    private lateinit var edtPesquisaNome: EditText
    private lateinit var edtPesquisaCidade: EditText
    private lateinit var adapter: ListaTrabalhadorAdapter
    private lateinit var recyclerViewCargos: RecyclerView
    private lateinit var cargoAdapter: CargoAdapter

    private val trabalhadorDao by lazy { AppDataBase.instancia(this).trabalhadorDao() }
    private val usuarioDao by lazy { AppDataBase.instancia(this).usuarioDao() }

    private val cargos = listOf(
        "Todos",
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

    private var cargoSelecionado: String? = null
    private var cidadeUsuario: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_trabalhador)

        val emailAutenticado = intent.getStringExtra("EMAIL") ?: ""

        recyclerView = findViewById(R.id.recyclerViewTrabalhadores)
        imageViewPerfilUsuario = findViewById(R.id.imageViewPerfilUsuario)
        edtPesquisaNome = findViewById(R.id.edtPesquisa)
        edtPesquisaCidade = findViewById(R.id.edtPesquisaCidade)
        recyclerViewCargos = findViewById(R.id.recyclerViewCargos)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ListaTrabalhadorAdapter(this, emptyList()) { trabalhador ->
            val intent = Intent(this, DetalhesTrabalhadorActivity::class.java)
            intent.putExtra("trabalhador", trabalhador)
            intent.putExtra("EMAIL", emailAutenticado)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        cargoAdapter = CargoAdapter(cargos) { cargo ->
            cargoSelecionado = if (cargo == "Todos") null else cargo
            buscarTrabalhadores(edtPesquisaNome.text.toString(), edtPesquisaCidade.text.toString())
        }
        recyclerViewCargos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewCargos.adapter = cargoAdapter

        val emailUsuario = intent.getStringExtra("EMAIL")
        emailUsuario?.let { email ->
            carregarFotoPerfil(email)
            buscarCidadeUsuario(email)
        }

        imageViewPerfilUsuario.setOnClickListener {
            val intent = Intent(this, PerfilUsuarioActivity::class.java)
            intent.putExtra("EMAIL", emailUsuario)
            startActivity(intent)
        }

        edtPesquisaNome.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                buscarTrabalhadores(s.toString(), edtPesquisaCidade.text.toString())
            }
        })

        edtPesquisaCidade.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                buscarTrabalhadores(edtPesquisaNome.text.toString(), s.toString())
            }
        })
    }

    override fun onResume() {
        super.onResume()
        buscarTrabalhadores(edtPesquisaNome.text.toString(), edtPesquisaCidade.text.toString())
        atualizarFotoPerfil()
    }

    private fun atualizarFotoPerfil() {
        val email = intent.getStringExtra("EMAIL") ?: return
        carregarFotoPerfil(email)
    }

    private fun carregarFotoPerfil(email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val usuario = usuarioDao.getUsuarioPorEmail(email)
            val nomeArquivo = usuario?.foto
            val uri = nomeArquivo?.let {
                ImagemURI.getImagemUriPeloNome(this@ListaTrabalhadorActivity, it)
            }
            runOnUiThread {
                if (uri != null) {
                    imageViewPerfilUsuario.setImageURI(uri)
                } else {
                    imageViewPerfilUsuario.setImageResource(R.drawable.imagem_padrao)
                }
            }
        }
    }

    private fun buscarCidadeUsuario(email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val usuario = usuarioDao.getUsuarioPorEmail(email)
            cidadeUsuario = usuario?.cidade ?: ""
        }
    }

    private fun buscarTrabalhadores(nomeQuery: String, cidadeQuery: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val resultado = trabalhadorDao.pesquisarTrabalhadores(
                if (nomeQuery.isBlank()) null else nomeQuery,
                if (cidadeQuery.isBlank()) null else cidadeQuery,
                cargoSelecionado
            )

            val listaOrdenada = resultado.sortedByDescending { trabalhador ->
                cidadeUsuario in trabalhador.cidade
            }

            runOnUiThread {
                adapter.atualiza(listaOrdenada)
            }
        }
    }
}