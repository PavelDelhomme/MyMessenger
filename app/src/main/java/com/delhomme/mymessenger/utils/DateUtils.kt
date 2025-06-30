package com.delhomme.mymessenger.utils

import java.text.SimpleDateFormat
import java.util.*

fun formatMessageDate(timestamp: Long, full: Boolean = false): String {
    val date = Date(timestamp)
    val now = Calendar.getInstance()
    val messageDate = Calendar.getInstance().apply { time = date }

    return if (full) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(date)
    } else {
        when {
            now.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR) ->
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            now.get(Calendar.DAY_OF_YEAR) - messageDate.get(Calendar.DAY_OF_YEAR) == 1 ->
                "Hier"
            now.get(Calendar.WEEK_OF_YEAR) == messageDate.get(Calendar.WEEK_OF_YEAR) ->
                SimpleDateFormat("EEE", Locale.getDefault()).format(date)
            else ->
                SimpleDateFormat("dd MMM", Locale.getDefault()).format(date)
        }
    }
}
