package com.example.travelwake.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.travelwake.data.model.BelongingEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BelongingsShareHelper {

    fun formatChecklistText(
        destinationName: String?,
        items: List<BelongingEntity>
    ): String {
        val dateFormat = SimpleDateFormat("EEEE, MMM d, yyyy • hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date())
        val completedCount = items.count { it.isChecked }
        val totalCount = items.size
        val percent = if (totalCount > 0) (completedCount * 100) / totalCount else 0

        val sb = StringBuilder()
        sb.append("📋 TRAVELWAKE PACKING & BELONGINGS CHECKLIST\n")
        sb.append("═══════════════════════════════════════════\n")
        if (!destinationName.isNullOrBlank()) {
            sb.append("🎯 Destination: $destinationName\n")
        }
        sb.append("📅 Generated: $dateStr\n")
        sb.append("📊 Progress: $completedCount of $totalCount packed ($percent%)\n")
        sb.append("═══════════════════════════════════════════\n\n")

        val grouped = items.groupBy { it.category.ifBlank { "Essentials" } }

        grouped.forEach { (category, categoryItems) ->
            sb.append("[$category]\n")
            categoryItems.forEach { item ->
                val checkMark = if (item.isChecked) "[✓]" else "[ ]"
                sb.append("  $checkMark ${item.name}\n")
            }
            sb.append("\n")
        }

        sb.append("═══════════════════════════════════════════\n")
        sb.append("Safe travels with TravelWake GPS Location Alarm!\n")
        return sb.toString()
    }

    fun shareViaMessaging(
        context: Context,
        destinationName: String?,
        items: List<BelongingEntity>
    ) {
        val text = formatChecklistText(destinationName, items)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "TravelWake Belongings Checklist - ${destinationName ?: "Trip"}")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Belongings Checklist")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun exportAsTextFile(
        context: Context,
        destinationName: String?,
        items: List<BelongingEntity>
    ): File? {
        return try {
            val text = formatChecklistText(destinationName, items)
            val fileName = "TravelWake_Belongings_Checklist_${System.currentTimeMillis()}.txt"
            val file = File(context.cacheDir, fileName)
            file.writeText(text)

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "TravelWake Belongings Checklist Export")
                putExtra(Intent.EXTRA_TEXT, "Here is my exported belongings checklist from TravelWake.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(sendIntent, "Export Checklist as Text File")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            Toast.makeText(context, "Exported checklist to text file!", Toast.LENGTH_SHORT).show()
            file
        } catch (e: Exception) {
            Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }
}
