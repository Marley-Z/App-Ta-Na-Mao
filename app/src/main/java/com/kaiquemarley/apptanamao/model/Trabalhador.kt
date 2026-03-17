package com.kaiquemarley.apptanamao.model

import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kaiquemarley.apptanamao.extensions.DataUtil
import kotlinx.parcelize.Parcelize

@Entity(
    indices = [Index(value = ["email"], unique = true)]  // email único
)
@Parcelize
data class Trabalhador(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    // id autoincrementável, padrão 0 para o Room gerar

    val email: String,
    val senha: String,
    val nome: String,
    val cpf: String,
    val dataNascimento: String,
    val cargo: String,
    val estado: String,
    val cidade: List<String>,
    val endereco: String,
    val telefone: String,
    val descricao: String? = null,
    val foto: String? = null,
) : Parcelable {

    @RequiresApi(Build.VERSION_CODES.O)
    fun idadeNumerica(): Int {
        return try {

            DataUtil.calcularIdade(dataNascimento)
        }
            catch(e: Exception) {
                0
            }
        }
    }


