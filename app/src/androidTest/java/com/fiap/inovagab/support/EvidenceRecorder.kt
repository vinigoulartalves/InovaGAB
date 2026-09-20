package com.fiap.inovagab.support

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onRoot
import androidx.test.platform.app.InstrumentationRegistry
import com.fiap.inovagab.MainActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * Grava PNG + índice JSON por caso. Não inclui credenciais nos metadados.
 */
object EvidenceRecorder {
    private val cases = JSONArray()

    fun gitCommit(): String =
        InstrumentationRegistry.getArguments().getString("gitCommit") ?: "unknown"

    fun evidenceRoot(): File {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        return File(ctx.getExternalFilesDir(null), "sprint2-evidence/${gitCommit()}").apply { mkdirs() }
    }

    fun record(
        rule: AndroidComposeTestRule<*, MainActivity>,
        caseId: String,
        screen: String,
        status: String,
        note: String = ""
    ) {
        val dir = evidenceRoot()
        val png = File(dir, "${caseId}__${screen}.png")
        rule.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 90, FileOutputStream(png))

        cases.put(
            JSONObject()
                .put("caseId", caseId)
                .put("screen", screen)
                .put("status", status)
                .put("commit", gitCommit())
                .put("file", png.name)
                .put("note", note)
        )
        flushIndex(dir)
    }

    fun flushIndex(dir: File = evidenceRoot()) {
        val index = JSONObject()
            .put("commit", gitCommit())
            .put("cases", cases)
        File(dir, "INDEX.json").writeText(index.toString(2))
    }
}
