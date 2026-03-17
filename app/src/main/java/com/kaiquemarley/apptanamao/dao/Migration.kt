package com.kaiquemarley.apptanamao.dao

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Criar tabela Usuario_novo (corrigindo schema)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS Usuario_novo (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                nome TEXT NOT NULL,
                email TEXT NOT NULL,
                senha TEXT NOT NULL,
                endereco TEXT NOT NULL,
                telefone TEXT NOT NULL,
                foto TEXT,
                estado TEXT NOT NULL,
                cidade TEXT NOT NULL
            )
        """.trimIndent())

        // Copiar dados da tabela antiga Usuario para Usuario_novo
        database.execSQL("""
            INSERT INTO Usuario_novo (nome, email, senha, endereco, telefone, foto, estado, cidade)
            SELECT nome, email, senha, endereco, telefone, foto, estado, cidade FROM Usuario
        """.trimIndent())

        // Substituir tabela antiga por nova
        database.execSQL("DROP TABLE Usuario")
        database.execSQL("ALTER TABLE Usuario_novo RENAME TO Usuario")

        // Recriar índice único para email
        database.execSQL("""
            CREATE UNIQUE INDEX IF NOT EXISTS index_Usuario_email ON Usuario (email)
        """.trimIndent())

        // Criar tabela Trabalhador temporária (mesma estrutura)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS Trabalhador_temp (
                id INTEGER NOT NULL PRIMARY KEY,
                email TEXT NOT NULL,
                senha TEXT NOT NULL,
                nome TEXT NOT NULL,
                dataNascimento TEXT NOT NULL,
                cargo TEXT NOT NULL,
                endereco TEXT NOT NULL,
                telefone TEXT NOT NULL,
                foto TEXT,
                descricao TEXT,
                cpf TEXT NOT NULL,
                estado TEXT NOT NULL,
                cidade TEXT NOT NULL
            )
        """.trimIndent())

        // Copiar dados da tabela Trabalhador para temporária
        database.execSQL("""
            INSERT INTO Trabalhador_temp (
                id, email, senha, nome, dataNascimento, cargo, endereco, telefone, foto, descricao, cpf, estado, cidade
            )
            SELECT id, email, senha, nome, dataNascimento, cargo, endereco, telefone, foto, descricao, cpf, estado, cidade
            FROM Trabalhador
        """.trimIndent())

        // Substituir tabela Trabalhador
        database.execSQL("DROP TABLE Trabalhador")
        database.execSQL("ALTER TABLE Trabalhador_temp RENAME TO Trabalhador")

        // Recriar índice único para email Trabalhador
        database.execSQL("""
            CREATE UNIQUE INDEX IF NOT EXISTS index_Trabalhador_email ON Trabalhador (email)
        """.trimIndent())

        // Criar nova tabela Disponibilidade com o schema correto (sem usuarioQueReservou, com usuarioId)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS Disponibilidade_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                trabalhadorId INTEGER,
                data TEXT,
                hora TEXT,
                reservada INTEGER NOT NULL DEFAULT 0,
                usuarioId INTEGER,
                FOREIGN KEY(trabalhadorId) REFERENCES Trabalhador(id) ON DELETE CASCADE,
                FOREIGN KEY(usuarioId) REFERENCES Usuario(id) ON DELETE SET NULL
            )
        """.trimIndent())

        // Copiar dados da tabela antiga para a nova
        // Coluna usuarioId é nova e inexistente na tabela antiga, então colocamos NULL
        database.execSQL("""
            INSERT INTO Disponibilidade_new (id, trabalhadorId, data, hora, reservada, usuarioId)
            SELECT id, trabalhadorId, data, hora, reservada, NULL FROM Disponibilidade
        """.trimIndent())

        // Apagar tabela antiga
        database.execSQL("DROP TABLE Disponibilidade")

        // Renomear tabela nova para original
        database.execSQL("ALTER TABLE Disponibilidade_new RENAME TO Disponibilidade")

        // Criar índices para a tabela nova
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_Disponibilidade_trabalhadorId ON Disponibilidade (trabalhadorId)
        """.trimIndent())

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_Disponibilidade_usuarioId ON Disponibilidade (usuarioId)
        """.trimIndent())
    }
}