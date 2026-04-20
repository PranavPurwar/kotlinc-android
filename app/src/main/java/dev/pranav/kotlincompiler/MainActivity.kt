package dev.pranav.kotlincompiler

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import java.io.*

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val input = findViewById<EditText>(R.id.commandInput)
        val outputView = findViewById<TextView>(R.id.outputView)
        val runBtn = findViewById<Button>(R.id.runButton)

        // Create a sample file once
        val sampleFile = File(cacheDir, "test.kt")
        if (!sampleFile.exists()) {
            sampleFile.writeText(
                """
                fun main() {
                    println("Hello from Kotlin CLI!")
                }
                """.trimIndent()
            )
        }

        runBtn.setOnClickListener {
            val command = input.text.toString()

            val result = when {
                command.startsWith("kotlinc ") -> {
                    runCompilerCommand(command.removePrefix("kotlinc "))
                }

                command == "ls" -> {
                    listFiles()
                }

                command == "help" -> {
                    helpText()
                }

                else -> "Unknown command. Type 'help'"
            }

            outputView.text = result
        }
    }

    // -----------------------------------
    // Run Kotlin compiler with user args
    // -----------------------------------
    private fun runCompilerCommand(command: String): String {
        return try {
            val args = parseArgs(command).toMutableList()

            // 🔒 Basic validation
            if (args.none { it.endsWith(".kt") }) {
                return "Error: No .kt file provided"
            }

            // Replace shortcuts like "test.kt"
            for (i in args.indices) {
                if (args[i].endsWith(".kt") && !args[i].startsWith("/")) {
                    args[i] = File(cacheDir, args[i]).absolutePath
                }
            }

            // Ensure output directory exists
            if (!args.contains("-d")) {
                args.add("-d")
                args.add(cacheDir.absolutePath)
            }

            val output = StringWriter()
            val writer = PrintWriter(output)

            val stream = PrintStream(object : OutputStream() {
                override fun write(p0: Int) {
                    writer.write(p0)
                }
            })


                val compiler = K2JVMCompiler()

            val exitCode = compiler.exec(
                stream,
                MessageRenderer.XML,
                *args.toTypedArray()
            )

            writer.flush()

            buildString {
                appendLine("Args: ${args.joinToString(" ")}")
                appendLine("Exit Code: $exitCode")
                appendLine("----- Output -----")
                append(output.toString())
            }

        } catch (e: Exception) {
            e.stackTraceToString()
        }
    }

    // -----------------------------------
    // Argument parser (supports quotes)
    // -----------------------------------
    private fun parseArgs(input: String): Array<String> {
        val regex = Regex("""[^\s"]+|"([^"]*)"""")
        return regex.findAll(input)
            .map { it.value.replace("\"", "") }
            .toList()
            .toTypedArray()
    }

    // -----------------------------------
    // List files in cache
    // -----------------------------------
    private fun listFiles(): String {
        return cacheDir.listFiles()
            ?.joinToString("\n") { it.name }
            ?: "Empty"
    }

    // -----------------------------------
    // Help text
    // -----------------------------------
    private fun helpText(): String {
        return """
Available commands:

kotlinc <args>     → Run Kotlin compiler
ls                 → List files
help               → Show this help

Examples:

kotlinc test.kt -d out
kotlinc test.kt -Xcontext-receivers
kotlinc "test.kt" -d "out dir"

Notes:
- test.kt is auto-resolved from cache directory
- Output directory defaults to cache if not provided
        """.trimIndent()
    }
}
