package com.kaiquemarley.apptanamao.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity ( indices = [Index(value = ["email"], unique = true)])

data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nome: String,
    val email: String,
    val senha: String,
    val estado: String,
    val cidade: String,
    val endereco: String,
    val telefone: String,
    val foto: String? = null,

)



