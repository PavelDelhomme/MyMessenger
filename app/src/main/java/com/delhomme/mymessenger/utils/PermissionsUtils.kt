package com.delhomme.mymessenger.utils

import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import android.util.Log

fun requestDefaultSmsApp(activity: Activity) {
    Log.d("PERMISSIONS", "Requesting default SMS app role")

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = activity.getSystemService(RoleManager::class.java)
            if (roleManager?.isRoleAvailable(RoleManager.ROLE_SMS) == true) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                    activity.startActivityForResult(intent, 1234)
                    Log.d("PERMISSIONS", "Started role request activity (API Q+)")
                } else {
                    Log.d("PERMISSIONS", "Role already held")
                }
            } else {
                Log.w("PERMISSIONS", "SMS role not available")
                fallbackToLegacyMethod(activity)
            }
        } else {
            fallbackToLegacyMethod(activity)
        }
    } catch (e: Exception) {
        Log.e("PERMISSIONS", "Error requesting default SMS app", e)
        fallbackToLegacyMethod(activity)
    }
}

private fun fallbackToLegacyMethod(activity: Activity) {
    Log.d("PERMISSIONS", "Using legacy method for SMS default request")
    try {
        val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
            putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, activity.packageName)
        }
        activity.startActivity(intent)
        Log.d("PERMISSIONS", "Started legacy SMS default activity")
    } catch (e: Exception) {
        Log.e("PERMISSIONS", "Error with legacy method", e)
    }
}

/**
 * Vérifie si l'application est l'application SMS par défaut
 * Utilise plusieurs méthodes pour être compatible avec toutes les versions Android
 */
fun isDefaultSmsApp(activity: Activity): Boolean {
    return try {
        // Méthode 1: Via RoleManager (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = activity.getSystemService(RoleManager::class.java)
            if (roleManager?.isRoleAvailable(RoleManager.ROLE_SMS) == true) {
                val isRoleHeld = roleManager.isRoleHeld(RoleManager.ROLE_SMS)
                Log.d("PERMISSIONS", "RoleManager check: $isRoleHeld")
                if (isRoleHeld) return true
            }
        }

        // Méthode 2: Via Telephony.Sms.getDefaultSmsPackage
        val defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(activity)
        val isDefault = defaultSmsApp == activity.packageName
        Log.d("PERMISSIONS", "Telephony check: $isDefault (current: $defaultSmsApp, ours: ${activity.packageName})")

        if (isDefault) return true

        // Méthode 3: Vérification via PackageManager pour les permissions spéciales
        val packageManager = activity.packageManager
        val hasWriteSmsPermission = packageManager.checkPermission(
            "android.permission.WRITE_SMS",
            activity.packageName
        ) == PackageManager.PERMISSION_GRANTED

        Log.d("PERMISSIONS", "WRITE_SMS permission check: $hasWriteSmsPermission")

        // Si on a la permission WRITE_SMS, on est probablement l'app par défaut
        hasWriteSmsPermission

    } catch (e: Exception) {
        Log.e("PERMISSIONS", "Error checking default SMS app status", e)
        false
    }
}