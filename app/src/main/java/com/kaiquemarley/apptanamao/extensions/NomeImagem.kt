package com.kaiquemarley.apptanamao.extensions

    object NomeImagem {
        fun gerarNomeArquivo(email: String): String {
            val emailSanitizado = email.replace("@", "_").replace(".", "_")
            val timestamp = System.currentTimeMillis()
            return "foto_${emailSanitizado}_$timestamp.jpg"
        }
    }


