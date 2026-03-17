package com.kaiquemarley.apptanamao.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.kaiquemarley.apptanamao.model.Disponibilidade
import com.kaiquemarley.apptanamao.model.Trabalhador

@Dao
interface DisponibilidadeDao {

    @Insert
    suspend fun inserir(disponibilidade: Disponibilidade)

    @Query("SELECT * FROM Disponibilidade WHERE trabalhadorId = :trabalhadorId")
    suspend fun getDisponibilidadesDoTrabalhador(trabalhadorId: Long): List<Disponibilidade>

    @Query("""
        UPDATE Disponibilidade 
        SET reservada = :reservada, 
            usuarioId = :usuarioId
        WHERE id = :id
    """)
    suspend fun atualizarReserva(
        id: Long,
        reservada: Boolean,
        usuarioId: Long?
    )


    @Delete
    fun deleta(disponibilidade: Disponibilidade)
}