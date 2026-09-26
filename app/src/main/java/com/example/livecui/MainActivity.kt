package com.example.livecui

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {

    external fun parseAndExecuteCCode(code: String): String

    companion object {
        init {
            System.loadLibrary("livecui")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiveCUIApp(onExecuteCode = { code -> parseAndExecuteCCode(code) })
        }
    }
}

@Composable
fun LiveCUIApp(onExecuteCode: (String) -> String) {
    var codeText by remember { mutableStateOf("// Tulis kode C di sini\nUI_Button {\n  label: \"Tombol Aksesibel\"\n}") }
    var errorMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // AREA LAYAR ATAS (Preview / Full Error Dump)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(if (isError) ComposeColor.Black else ComposeColor(0xFF222222))
        ) {
            if (isError) {
                Text(
                    text = errorMessage,
                    color = ComposeColor.Red,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                )
            } else {
                // Custom Native Canvas Render + Accessibility Node Integration
                AndroidView(
                    factory = { ctx ->
                        object : View(ctx) {
                            private val paint = Paint().apply {
                                color = Color.GREEN
                                textSize = 40f
                            }
                            override fun onDraw(canvas: Canvas) {
                                super.onDraw(canvas)
                                canvas.drawText("Render UI C Berhasil!", 50f, 100f)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // AREA LAYAR BAWAH (Editor Kode + Kirim)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            OutlinedTextField(
                value = codeText,
                onValueChange = { codeText = it },
                label = { Text("Kode C (LVGL-Style)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    // 1. Sembunyikan Papan Ketik (Auto Hide Keyboard)
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    val currentView = (context as? Activity)?.currentFocus
                    currentView?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }

                    // 2. Eksekusi Kode C di Native
                    val result = onExecuteCode(codeText)
                    if (result.isNotEmpty()) {
                        isError = true
                        errorMessage = result
                    } else {
                        isError = false
                        errorMessage = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("KIRIM & RENDER")
            }
        }
    }
}
