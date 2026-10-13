package com.example.cifrabook

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object RecentManager {
    private const val PREF_NAME = "cifrabook_prefs"
    private const val KEY_RECENTS = "recent_cifras"

    fun addRecent(context: Context, uriString: String) {
        if (uriString.isBlank() || uriString.contains("msf:")) return

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val recents = getRecents(context).toMutableList()

        recents.remove(uriString)
        recents.add(0, uriString)

        if (recents.size > 15) {
            recents.removeAt(recents.size - 1)
        }

        val json = Gson().toJson(recents)
        prefs.edit().putString(KEY_RECENTS, json).apply()
    }

    fun getRecents(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_RECENTS, null) ?: return emptyList()

        return try {
            val type = object : TypeToken<List<String>>() {}.type
            val listaBruta = Gson().fromJson<List<String>>(json, type) ?: emptyList()

            // FILTRAGEM CIRÚRGICA: Remove permanentemente qualquer item com "msf:" e salva a lista limpa
            val listaLimpa = listaBruta.filter { !it.isNullOrBlank() && !it.contains("msf:") }

            // Se a lista limpa for diferente da bruta (porque continha "msf:"), atualiza o SharedPreferences na hora
            if (listaLimpa.size != listaBruta.size) {
                val novoJson = Gson().toJson(listaLimpa)
                prefs.edit().putString(KEY_RECENTS, novoJson).apply()
            }

            listaLimpa
        } catch (e: Exception) {
            prefs.edit().clear().apply()
            emptyList()
        }
    }
}