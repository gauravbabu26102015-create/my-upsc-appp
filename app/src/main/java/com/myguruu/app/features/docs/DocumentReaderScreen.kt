package com.myguruu.app.features.docs

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentReaderScreen() {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isPdf by remember { mutableStateOf(false) }

    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val type = context.contentResolver.getType(it)
            isPdf = type == "application/pdf"
            selectedUri = it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Document Reader") },
                actions = {
                    IconButton(onClick = { documentLauncher.launch("application/pdf") }) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = "Open PDF")
                    }
                    IconButton(onClick = {
                        isPdf = false
                        selectedUri = Uri.parse("https://upsc.gov.in") // Default HTML view
                    }) {
                        Icon(Icons.Filled.Web, contentDescription = "Open Web")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (selectedUri == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Select a PDF or tap Web to open materials.")
                }
            } else {
                if (isPdf) {
                    PdfViewer(uri = selectedUri!!, context = context)
                } else {
                    HtmlViewer(url = selectedUri!!.toString())
                }
            }
        }
    }
}

@Composable
fun PdfViewer(uri: Uri, context: Context) {
    var pdfBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            try {
                // Copy URI to local file for PdfRenderer
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "temp_doc.pdf")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val pdfRenderer = PdfRenderer(fileDescriptor)
                val pageCount = pdfRenderer.pageCount
                val bitmaps = mutableListOf<Bitmap>()

                for (i in 0 until pageCount) {
                    val page = pdfRenderer.openPage(i)
                    val bitmap = Bitmap.createBitmap(
                        page.width * 2,
                        page.height * 2,
                        Bitmap.Config.ARGB_8888
                    )
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmaps.add(bitmap)
                    page.close()
                }

                pdfRenderer.close()
                fileDescriptor.close()

                withContext(Dispatchers.Main) {
                    pdfBitmaps = bitmaps
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (pdfBitmaps.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(pdfBitmaps.size) { index ->
                Image(
                    bitmap = pdfBitmaps[index].asImageBitmap(),
                    contentDescription = "PDF Page ${index + 1}",
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun HtmlViewer(url: String) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                loadUrl(url)
            }
        },
        update = { webView ->
            webView.loadUrl(url)
        }
    )
}
