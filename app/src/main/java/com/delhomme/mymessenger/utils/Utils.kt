package com.delhomme.mymessenger.utils

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.google.i18n.phonenumbers.PhoneNumberUtil


fun lookupName(context: Context, number: String): String {
    val uri = Uri.withAppendedPath(
        ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
        Uri.encode(number)
    )
    context.contentResolver.query(
        uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
        null, null, null
    )?.use { c ->
        if (c.moveToFirst()) return c.getString(0)
    }
    return number
}

fun lookupContact(context: Context, number: String): Pair<String, String?> {
    val uri = Uri.withAppendedPath(
        ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
        Uri.encode(number)
    )
    context.contentResolver.query(
        uri,
        arrayOf(
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI
        ),
        null, null, null
    )?.use { c ->
        if (c.moveToFirst()) {
            val name = c.getString(0)
            val photo = c.getString(1)
            return name to photo
        }
    }
    return number to null
}

private var contactsCache: List<Pair<String, String>>? = null


fun getAllContacts(context: Context): List<Pair<String, String>> {
    contactsCache?.let { return it }

    val cursor = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        ),
        null, null, "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} COLLATE NOCASE ASC"
    ) ?: return emptyList()

    return try {
        buildList {
            while (cursor.moveToNext()) {
                val number = cursor.getString(0)?.trim() ?: ""
                val name = cursor.getString(1)?.trim() ?: ""

                // Filtrage des entrées vides
                if (number.isNotBlank() && name.isNotBlank()) {
                    add(number to name)
                }
            }
        }.distinctBy { it.first }.also {
            contactsCache = it // Mise en cache
        }
    } finally {
        cursor.close()
    }
}


fun formatFrenchPhoneNumber(number: String): String {
    val digits = number.filter { it.isDigit() }
    return when {
        digits.length == 10 && digits.startsWith('0') ->
            "${digits.substring(0, 2)} ${digits.substring(2, 4)} ${digits.substring(4, 6)} ${digits.substring(6, 8)} ${digits.substring(8, 10)}"
        digits.length == 9 && digits.startsWith('1') ->
            "${digits.substring(0, 1)} ${digits.substring(1, 3)} ${digits.substring(3, 5)} ${digits.substring(5, 7)} ${digits.substring(7, 9)}"
        else -> number
    }
}


fun normalizePhoneNumber(number: String): String {
    val phoneUtil = PhoneNumberUtil.getInstance()
    return try {
        val parsedNumber = phoneUtil.parse(number, null)
        phoneUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
    } catch (e: Exception) {
        // Fallback pour les numéros invalides
        val digits = number.filter { it.isDigit() }
        when {
            digits.startsWith("33") && digits.length == 11 -> "+$digits"
            digits.startsWith("0") && digits.length == 10 -> "+33${digits.substring(1)}"
            digits.startsWith("+") -> number
            else -> "+$digits"
        }
    }
}