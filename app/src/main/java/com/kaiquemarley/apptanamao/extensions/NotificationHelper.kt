package com.kaiquemarley.apptanamao.extensions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kaiquemarley.apptanamao.activity.AdicionarDataActivity
import com.kaiquemarley.apptanamao.activity.DetalhesTrabalhadorActivity

object NotificationHelper {

    private const val CHANNEL_ID = "reservas_channel"
    private const val CHANNEL_NAME = "Reservas"
    private const val NOTIFICATION_ID = 1001

    fun criarCanalNotificacao(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    fun notificarReservaConfirmada(context: Context, trabalhadorId: Long, mensagem: String) {
        criarCanalNotificacao(context)

        val intent = Intent(context, AdicionarDataActivity::class.java).apply {
            putExtra("TRABALHADOR_ID", trabalhadorId)
        }

        val stackBuilder = TaskStackBuilder.create(context).apply {
            // Define a pilha: PerfilTrabalhadorActivity -> AdicionarDataActivity
            addNextIntentWithParentStack(intent)
        }

        val pendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Nova Reserva")
            .setContentText(mensagem)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, builder.build())

    }

    fun notificarReservaCancelada(context: Context, trabalhadorId: Long, mensagem: String) {
        criarCanalNotificacao(context)

        val intent = Intent(context, DetalhesTrabalhadorActivity::class.java).apply {
            putExtra("TRABALHADOR_ID", trabalhadorId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_delete)  // Ícone diferente para cancelamento
            .setContentTitle("Reserva Cancelada pelo Trabalhador")
            .setContentText(mensagem)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID + 1, builder.build())  // ID diferente se quiser
    }

    fun notificarTrabalhadorReservaCancelada(context: Context, trabalhadorId: Long, mensagem: String) {
        criarCanalNotificacao(context)

        val intent = Intent(context, DetalhesTrabalhadorActivity::class.java).apply {
            putExtra("TRABALHADOR_ID", trabalhadorId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_delete)
            .setContentTitle("Reserva Cancelada pelo Usuário")
            .setContentText(mensagem)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID + 2, builder.build())  // ID diferente
    }
}