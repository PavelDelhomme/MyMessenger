package com.delhomme.mymessenger.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun formatMessageDate(timestamp: Long, full: Boolean = false): String {
    val date = Date(timestamp)
    val now = Calendar.getInstance()
    val messageDate = Calendar.getInstance().apply { time = date }

    return if (full) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(date)
    } else {
        when {
            // Même jour
            now.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR) &&
                    now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) ->
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)

            // Hier
            now.get(Calendar.DAY_OF_YEAR) - messageDate.get(Calendar.DAY_OF_YEAR) == 1 &&
                    now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) ->
                "Hier"

            // Cette semaine
            now.get(Calendar.WEEK_OF_YEAR) == messageDate.get(Calendar.WEEK_OF_YEAR) &&
                    now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) ->
                SimpleDateFormat("EEE", Locale.FRENCH).format(date)

            // Cette année
            now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) ->
                SimpleDateFormat("dd MMM", Locale.FRENCH).format(date)

            // Autre année
            else ->
                SimpleDateFormat("dd/MM/yy", Locale.FRENCH).format(date)
        }
    }
}

fun formatConversationDate(timestamp: Long): String {
    val now = Calendar.getInstance()
    val date = Date(timestamp)
    val messageDate = Calendar.getInstance().apply { time = date }

    val diffMillis = now.timeInMillis - timestamp
    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

    return when {
        // Aujourd'hui (même jour calendaire)
        now.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR) &&
                now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) -> {
            SimpleDateFormat("HH:mm", Locale.FRENCH).format(date)
        }

        // Hier
        diffDays == 1L || (
                now.get(Calendar.DAY_OF_YEAR) - messageDate.get(Calendar.DAY_OF_YEAR) == 1 &&
                        now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR)
                ) -> "Hier"

        // Cette semaine (2-6 jours)
        diffDays <= 6L &&
                now.get(Calendar.WEEK_OF_YEAR) == messageDate.get(Calendar.WEEK_OF_YEAR) &&
                now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) -> {
            SimpleDateFormat("EEE", Locale.FRENCH).format(date)
        }

        // Cette année
        now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) -> {
            SimpleDateFormat("dd MMM", Locale.FRENCH).format(date)
        }

        // Autre année
        else -> {
            SimpleDateFormat("dd/MM/yy", Locale.FRENCH).format(date)
        }
    }
}
