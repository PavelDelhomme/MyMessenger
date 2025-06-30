package com.delhomme.mymessenger.utils

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

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
    if (contactsCache != null) return contactsCache!!

    val list = mutableListOf<Pair<String, String>>()
    val cursor = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        ),
        null, null, null
    )

    cursor?.use {
        while (it.moveToNext()) {
            list += it.getString(0) to it.getString(1)
        }
    }

    contactsCache = list.distinctBy { it.first }
    return contactsCache!!
}