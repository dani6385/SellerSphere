package com.example.ui.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.model.Product
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportUtil {

    fun generateLowStockPdfReport(
        context: Context,
        lowStockProducts: List<Product>,
        onProgress: (Float) -> Unit
    ): Uri? {
        // Step 1: Initialize document
        onProgress(0.1f)
        val pdfDocument = PdfDocument()
        
        // Page dimensions: A4 size (595 x 842 points)
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        // Step 2: Draw content on Canvas
        onProgress(0.3f)
        
        val titlePaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        
        val subtitlePaint = Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }
        
        val headerPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        
        val bodyPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        
        val boldBodyPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val redBodyPaint = Paint().apply {
            color = android.graphics.Color.RED
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val linePaint = Paint().apply {
            color = android.graphics.Color.LTGRAY
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        
        val bgPaint = Paint().apply {
            color = android.graphics.Color.rgb(240, 244, 248)
            style = Paint.Style.FILL
        }

        val footerBgPaint = Paint().apply {
            color = android.graphics.Color.rgb(245, 245, 245)
            style = Paint.Style.FILL
        }
        
        // Top banner color band (Teal accent color)
        canvas.drawRect(30f, 30f, 565f, 35f, Paint().apply { color = android.graphics.Color.rgb(10, 150, 180) })
        
        // Header
        canvas.drawText("SS SELLER SPHERE", 30f, 60f, titlePaint)
        canvas.drawText("LAPORAN RINGKASAN STOK MENIPIS", 30f, 85f, Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        })
        
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale("id", "ID"))
        val dateStr = sdf.format(Date())
        canvas.drawText("Tanggal Dibuat: $dateStr", 30f, 105f, subtitlePaint)
        
        // Divider line
        canvas.drawLine(30f, 120f, 565f, 120f, Paint().apply { color = android.graphics.Color.BLACK; strokeWidth = 1.5f })
        
        // Summary Card Box
        canvas.drawRect(30f, 135f, 565f, 185f, bgPaint)
        canvas.drawText("RINGKASAN STATUS INVENTARIS:", 45f, 153f, boldBodyPaint)
        canvas.drawText("Total item dalam sistem yang memerlukan restock segera: ${lowStockProducts.size} item.", 45f, 172f, redBodyPaint)
        
        // Table Columns Header
        val startY = 210f
        canvas.drawRect(30f, startY, 565f, startY + 25f, footerBgPaint)
        canvas.drawText("NAMA PRODUK", 35f, startY + 17f, headerPaint)
        canvas.drawText("SKU", 210f, startY + 17f, headerPaint)
        canvas.drawText("KATEGORI", 310f, startY + 17f, headerPaint)
        canvas.drawText("MIN", 420f, startY + 17f, headerPaint)
        canvas.drawText("STOK SEKARANG", 470f, startY + 17f, headerPaint)
        
        canvas.drawLine(30f, startY, 565f, startY, Paint().apply { color = android.graphics.Color.BLACK; strokeWidth = 1f })
        canvas.drawLine(30f, startY + 25f, 565f, startY + 25f, Paint().apply { color = android.graphics.Color.BLACK; strokeWidth = 1f })
        
        // Table Rows
        var currentY = startY + 25f
        lowStockProducts.forEachIndexed { index, prod ->
            currentY += 22f
            
            // Zebra striping background
            if (index % 2 == 1) {
                canvas.drawRect(30f, currentY - 17f, 565f, currentY + 5f, Paint().apply { color = android.graphics.Color.rgb(250, 252, 254) })
            }
            
            val nameTruncated = if (prod.name.length > 28) prod.name.take(25) + "..." else prod.name
            canvas.drawText(nameTruncated, 35f, currentY, bodyPaint)
            canvas.drawText(if (prod.sku.isBlank()) "-" else prod.sku, 210f, currentY, bodyPaint)
            canvas.drawText(prod.category, 310f, currentY, bodyPaint)
            canvas.drawText(prod.minStockThreshold.toString(), 420f, currentY, bodyPaint)
            canvas.drawText(prod.stock.toString(), 470f, currentY, redBodyPaint)
            
            // Grid Divider
            canvas.drawLine(30f, currentY + 5f, 565f, currentY + 5f, linePaint)
        }
        
        // Footer notes
        canvas.drawLine(30f, 780f, 565f, 780f, linePaint)
        canvas.drawText("Laporan ini dibuat otomatis oleh sistem SS Seller Sphere untuk mendukung efisiensi operasional.", 30f, 795f, subtitlePaint)
        canvas.drawText("Dokumen Resmi Digital - Tidak memerlukan tanda tangan basah.", 30f, 810f, subtitlePaint)
        
        pdfDocument.finishPage(page)
        
        // Step 3: Write PDF to Storage
        onProgress(0.6f)
        val filename = "Laporan_Stok_Menipis_${System.currentTimeMillis()}.pdf"
        var outputStream: OutputStream? = null
        var fileUri: Uri? = null
        
        try {
            // First we write a local copy inside external files dir or cache so FileProvider can serve it directly
            val cacheFile = File(context.cacheDir, filename)
            outputStream = FileOutputStream(cacheFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            outputStream = null
            
            // Get FileProvider Uri for opening
            fileUri = FileProvider.getUriForFile(
                context,
                "com.example.fileprovider",
                cacheFile
            )

            // Let's also copy/export the PDF to the public Downloads folder so it is easily "downloadable" by the user
            onProgress(0.8f)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val downloadUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (downloadUri != null) {
                    val publicOut = resolver.openOutputStream(downloadUri)
                    if (publicOut != null) {
                        val input = cacheFile.inputStream()
                        input.copyTo(publicOut)
                        input.close()
                        publicOut.flush()
                        publicOut.close()
                    }
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                val publicFile = File(downloadsDir, filename)
                val publicOut = FileOutputStream(publicFile)
                val input = cacheFile.inputStream()
                input.copyTo(publicOut)
                input.close()
                publicOut.flush()
                publicOut.close()
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            fileUri = null
        } finally {
            try {
                outputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            pdfDocument.close()
        }
        
        onProgress(1.0f)
        return fileUri
    }
}
