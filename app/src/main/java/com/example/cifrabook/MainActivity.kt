package com.example.cifrabook

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream

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
                // Ignora se não for suportado
            }

            // Copia o PDF para o armazenamento interno da app de forma segura
            val arquivoInternoUri = copiarUriParaArmazenamentoInterno(uri)

            if (arquivoInternoUri != null) {
                RecentManager.addRecent(this, arquivoInternoUri.toString())
                abrirLeitorPdf(arquivoInternoUri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = "O seu livro de Cifras"

        recyclerViewCifras = findViewById(R.id.recyclerViewCifras)
        recyclerViewCifras.layoutManager = GridLayoutManager(this, 2)

        val fabAddPdf = findViewById<FloatingActionButton>(R.id.fabAddPdf)
        fabAddPdf.setOnClickListener {
            selecionarPdfLauncher.launch(arrayOf("application/pdf"))
        }
    }

    override fun onResume() {
        super.onResume()

        // BUSCA OS RECENTES E OCULTA QUALQUER COISA COM "msf:" DE FORMA SEGURA
        val listaBruta = RecentManager.getRecents(this)
        val listaCifrasRecentes = listaBruta.filter {
            !it.isNullOrBlank() && !it.contains("msf:")
        }.take(15)

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

    private fun copiarUriParaArmazenamentoInterno(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null

            var nomeArquivo = "cifra_${System.currentTimeMillis()}.pdf"
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        val nomeReal = it.getString(nameIndex)
                        if (!nomeReal.isNullOrEmpty()) {
                            nomeArquivo = nomeReal
                        }
                    }
                }
            }

            val file = File(filesDir, nomeArquivo)
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Adapter atualizado
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
                setBackgroundColor(0xFF2D2D2D.toInt())
                setPadding(16, 24, 16, 24)
            }
            return ViewHolder(button)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val uriString = items[position]
            val uri = Uri.parse(uriString)
            var nomeCifra = uri.lastPathSegment ?: "Cifra ${position + 1}"

            if (nomeCifra.endsWith(".pdf", ignoreCase = true)) {
                nomeCifra = nomeCifra.substring(0, nomeCifra.length - 4)
            }

            if (nomeCifra.length > 17) {
                nomeCifra = nomeCifra.take(17) + "..."
            }

            holder.btnCifra.text = nomeCifra
            holder.btnCifra.setOnClickListener { onClick(uriString) }
        }

        override fun getItemCount(): Int = items.size
    }
}