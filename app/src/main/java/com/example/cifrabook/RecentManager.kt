package com.example.cifrabook

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object RecentManager {
    private const val PREF_NAME = "cifrabook_prefs"
    private const val KEY_RECENTS = "recent_cifras"

    // Salva uma URI na lista de recentes (mantém no máximo 15 itens)
    fun addRecent(context: Context, uriString: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val recents = getRecents(context).toMutableList()

        // Remove se já existir para colocar no topo
        recents.remove(uriString)
        recents.add(0, uriString)

        // Atualizado de 5 para 15 itens recentes
        if (recents.size > 15) {
            recents.removeAt(recents.size - 1)
        }

        val json = Gson().toJson(recents)
        prefs.edit().putString(KEY_RECENTS, json).apply()
    }

    // Retorna a lista de URIs recentes
    fun getRecents(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_RECENTS, null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(json, type)
    }
}