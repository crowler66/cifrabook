package com.example.cifrabook

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
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

        // Define o título da tela ou barra superior como "O seu livro de cifras"
        supportActionBar?.title = "O seu livro de Cifras"

        recyclerViewCifras = findViewById(R.id.recyclerViewCifras)

        // Grade com 2 colunas para os botões se espalharem bem pela tela
        recyclerViewCifras.layoutManager = GridLayoutManager(this, 2)

        val fabAddPdf = findViewById<FloatingActionButton>(
            R.id.fabAddPdf
        )

        fabAddPdf.setOnClickListener {
            selecionarPdfLauncher.launch(arrayOf("application/pdf"))
        }
    }

    override fun onResume() {
        super.onResume()

        // Pega as cifras recentes e aumenta o limite para até 15 itens
        val listaCifrasRecentes = RecentManager.getRecents(this).take(15)

        // Liga a lista ao RecyclerView usando o adapter de botões em grade
        recyclerViewCifras.adapter = RecentesBotoesAdapter(listaCifrasRecentes) { uriString ->
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

    // Adapter com botões estilizados, limite de 15 itens e até 17 caracteres no nome
    class RecentesBotoesAdapter(
        private val items: List<String>,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<RecentesBotoesAdapter.ViewHolder>() {

        class ViewHolder(val btnCifra: Button) : RecyclerView.ViewHolder(btnCifra)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val context = parent.context
            val button = Button(context).apply {
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(10, 10, 10, 10)
                }
                isAllCaps = false
                textSize = 14f
                setTextColor(0xFFFFFFFF.toInt())
                setBackgroundColor(0xFF2D2D2D.toInt()) // Tom escuro elegante
                setPadding(16, 24, 16, 24)
            }
            return ViewHolder(button)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val uriString = items[position]
            val uri = Uri.parse(uriString)

            var nomeCifra = "Cifra"
            val cursor = holder.itemView.context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        nomeCifra = it.getString(nameIndex) ?: "Cifra"
                    }
                }
            }

            if (nomeCifra == "Cifra" || nomeCifra.isEmpty()) {
                nomeCifra = uri.lastPathSegment ?: "Cifra"
                val idx = nomeCifra.lastIndexOf('/')
                if (idx != -1) nomeCifra = nomeCifra.substring(idx + 1)
            }

            // Remove a extensão .pdf
            if (nomeCifra.endsWith(".pdf", ignoreCase = true)) {
                nomeCifra = nomeCifra.substring(0, nomeCifra.length - 4)
            }

            // Limita o nome a 17 caracteres para manter o botão organizado
            if (nomeCifra.length > 17) {
                nomeCifra = nomeCifra.take(17) + "..."
            }

            holder.btnCifra.text = nomeCifra
            holder.btnCifra.setOnClickListener { onClick(uriString) }
        }

        override fun getItemCount(): Int = items.size
    }
}