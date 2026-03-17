package com.kaiquemarley.apptanamao.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kaiquemarley.apptanamao.converters.Converters
import com.kaiquemarley.apptanamao.model.Disponibilidade
import com.kaiquemarley.apptanamao.model.Trabalhador
import com.kaiquemarley.apptanamao.model.Usuario

@Database(entities = [Trabalhador::class, Usuario::class, Disponibilidade::class], version = 9)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {

    abstract fun trabalhadorDao(): TrabalhadorDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun disponibilidadeDao(): DisponibilidadeDao

    companion object {
        @Volatile
        private var db: AppDataBase? = null

        fun instancia(context: Context): AppDataBase {
            return db ?: Room.databaseBuilder(
                context,
                AppDataBase::class.java,
                "apptanamao.db"
            ).addMigrations(MIGRATION_8_9)
                .allowMainThreadQueries()
                .build().also { db = it }
        }
    }
}