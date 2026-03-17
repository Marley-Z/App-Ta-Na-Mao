package com.kaiquemarley.apptanamao.extensions

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

object DataUtil {
    @RequiresApi(Build.VERSION_CODES.O)
    fun calcularIdade(dataNascimento: String): Int {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val nascimento = LocalDate.parse(dataNascimento, formatter)
        val hoje = LocalDate.now()
        return Period.between(nascimento, hoje).years
    }
}
