package com.kaiquemarley.apptanamao.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kaiquemarley.apptanamao.model.Usuario

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) // ABORT lança exceção se o email já existir
    fun salvar(usuario: Usuario)

    @Query("SELECT * FROM Usuario WHERE email = :email AND senha = :senha")
    suspend fun autenticar(email: String, senha: String): Usuario?

    @Query("SELECT * FROM Usuario WHERE email = :email LIMIT 1")
    suspend fun getUsuarioPorEmail(email: String): Usuario?

    @Query("SELECT * FROM Usuario WHERE email = :email LIMIT 1")
    fun buscaUsuarioPorEmail(email: String): Usuario?


        @Query("SELECT * FROM Usuario WHERE id = :usuarioId LIMIT 1")
        fun buscaUsuarioPorId(usuarioId: Long): Usuario?

        // ... outros métodos, como inserir, atualizar etc.


    @Update
    fun atualiza(usuario: Usuario)

    @Delete
    fun deleta(usuario: Usuario)

}
