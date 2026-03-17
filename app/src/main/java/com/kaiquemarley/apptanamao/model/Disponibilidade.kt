package com.kaiquemarley.apptanamao.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.ColumnInfo

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Trabalhador::class,
            parentColumns = ["id"],
            childColumns = ["trabalhadorId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Disponibilidade(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(index = true)
    val trabalhadorId: Long? = null,

    val data: String? = null,
    val hora: String? = null,

    var reservada: Boolean = false,

    @ColumnInfo(index = true)
    var usuarioId: Long? = null
)