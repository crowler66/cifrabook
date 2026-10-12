package com.example.cifrabook

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerViewCifras: RecyclerView

    private val selecionarPdfLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Continua mesmo se a permissão persistente não for concedida.
            }

            abrirLeitorPdf(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerViewCifras = findViewById(R.id.recyclerViewCifras)
        recyclerViewCifras.layoutManager = LinearLayoutManager(this)

        val fabAddPdf = findViewById<FloatingActionButton>(
            R.id.fabAddPdf
        )

        fabAddPdf.setOnClickListener {
            selecionarPdfLauncher.launch(arrayOf("application/pdf"))
        }
    }

    override fun onResume() {
        super.onResume()

        // Busca a lista de cifras recentes salvas no aparelho
        val listaCifrasRecentes = RecentManager.getRecents(this)

        // Liga a lista ao RecyclerView da tela principal
        recyclerViewCifras.adapter = RecentesAdapter(listaCifrasRecentes) { uriString ->
            val uri = Uri.parse(uriString)
            abrirLeitorPdf(uri)
        }
    }

    private fun abrirLeitorPdf(uri: Uri) {
        val intent = Intent(this, PdfViewerActivity::class.java).apply {
            putExtra("PDF_URI", uri.toString())
        }
        startActivity(intent)
    }

    // Adapter para desenhar as cifras recentes na lista principal com o nome real do arquivo
    class RecentesAdapter(
        private val items: List<String>,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<RecentesAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvItem: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val uriString = items[position]
            val uri = Uri.parse(uriString)

            // Tenta obter o nome real do arquivo PDF através do ContentResolver
            var nomeCifra = "Cifra Recente"
            val cursor = holder.itemView.context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        nomeCifra = it.getString(nameIndex)
                    }
                }
            }

            // Se falhar a consulta, recorre ao método alternativo
            if (nomeCifra == "Cifra Recente" || nomeCifra.isEmpty()) {
                nomeCifra = uri.lastPathSegment ?: "Cifra Recente"
                val idx = nomeCifra.lastIndexOf('/')
                if (idx != -1) nomeCifra = nomeCifra.substring(idx + 1)
            }

            holder.tvItem.text = nomeCifra
            holder.tvItem.setTextColor(0xFFFFFFFF.toInt()) // Texto branco para combinar com o fundo escuro
            holder.itemView.setOnClickListener { onClick(uriString) }
        }

        override fun getItemCount(): Int = items.size
    }
}