package com.kaiquemarley.apptanamao.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kaiquemarley.apptanamao.model.Trabalhador


@Dao
interface TrabalhadorDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun salvar(trabalhador: Trabalhador): Long

    @Query("SELECT * FROM Trabalhador WHERE email = :email AND senha = :senha")
    suspend fun autenticar(email: String, senha: String): Trabalhador?

    @Query("SELECT * FROM Trabalhador WHERE email = :email LIMIT 1")
    suspend fun getTrabalhadorPorEmail(email: String): Trabalhador?

    @Query("SELECT * FROM Trabalhador WHERE email = :email LIMIT 1")
    fun buscaTrabalhadorPorEmail(email: String): Trabalhador?

    @Query("SELECT * FROM Trabalhador WHERE email = :email AND id != :id LIMIT 1")
    suspend fun buscaOutroTrabalhadorComEmail(email: String, id: Long): Trabalhador?

    @Query("SELECT * FROM trabalhador")
    suspend fun buscaTodos(): List<Trabalhador>

    @Query("SELECT * FROM trabalhador WHERE nome LIKE '%' || :query || '%'")
    suspend fun pesquisarPorNome(query: String): List<Trabalhador>

    @Query("SELECT * FROM trabalhador WHERE cargo = :cargo")
    suspend fun buscarPorCargo(cargo: String): List<Trabalhador>

    @Query("SELECT * FROM trabalhador WHERE nome LIKE '%' || :nome || '%' AND cargo = :cargo")
    suspend fun pesquisarPorNomeECargo(nome: String, cargo: String): List<Trabalhador>


    @Query("""
    SELECT * FROM Trabalhador
    WHERE (:nome IS NULL OR nome LIKE '%' || :nome || '%')
      AND (:cidade IS NULL OR cidade  LIKE '%' || :cidade || '%')
      AND (:cargo IS NULL OR cargo = :cargo)
""")
    suspend fun pesquisarTrabalhadores(
        nome: String?,
        cidade: String?,
        cargo: String?
    ): List<Trabalhador>

    @Update
    fun atualiza(trabalhador: Trabalhador)

    @Delete
    fun deleta(trabalhador: Trabalhador)
}
