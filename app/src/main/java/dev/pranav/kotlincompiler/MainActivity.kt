package dev.pranav.kotlincompiler

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sun.tools.javac.ConfigProvider
import com.sun.tools.javac.main.JavaCompiler
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import java.io.OutputStream
import java.io.PrintStream
import java.io.PrintWriter
import java.io.StringWriter

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

        runBtn.setOnClickListener {
            val command = input.text.toString()

            val result = when {
                command.startsWith("javac ") -> {
                    runJavac(command.removePrefix("javac "))
                }

                command.startsWith("kotlinc ") -> {
                    runKotlinc(command.removePrefix("kotlinc "))
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

    private fun runKotlinc(command: String): String {
        return try {
            val args = parseArgs(command).toMutableList()

            val output = StringWriter()
            val writer = PrintWriter(output)

            val stream = PrintStream(object: OutputStream() {
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

    private fun runJavac(command: String): String {
        return try {

            val args = parseArgs(command).toMutableList()

            val cleanedArgs = mutableListOf<String>()

            var i = 0
            while (i < args.size) {
                when (args[i]) {

                    "-javaHome" -> {
                        if (i + 1 < args.size) {
                            val path = args[i + 1]
                            ConfigProvider.setJavaHome(path)
                            i += 2
                            continue
                        }
                    }

                    else -> cleanedArgs.add(args[i])
                }
                i++
            }

            val output = StringWriter()
            val writer = PrintWriter(output)

            com.sun.tools.javac.Main.compile(
                cleanedArgs.toTypedArray(),
                writer
            )

            writer.flush()

            buildString {
                appendLine("JAVA_HOME = ${ConfigProvider.getJavaHome()}")
                appendLine("Args: ${cleanedArgs.joinToString(" ")}")
                appendLine("----- Output -----")
                append(output.toString())
            }

        } catch (e: Exception) {
            e.stackTraceToString()
        }
    }

    private fun parseArgs(input: String): Array<String> {
        val regex = Regex("""[^\s"]+|"([^"]*)"""")
        return regex.findAll(input)
            .map { it.value.replace("\"", "") }
            .toList()
            .toTypedArray()

    }

    private fun listFiles(): String {
        return cacheDir.listFiles()
            ?.joinToString("\n") { it.name }
            ?: "Empty"
    }

    private fun helpText(): String {
        return """
Available commands:

${JavaCompiler.fullVersion()}

javac <args>       → Run Java compiler
kotlinc <args>     → Run Kotlinc compiler
ls                 → List files
help               → Show this help

Examples:

javac -javaHome <path_to_home> --version
kotlinc --version

Notes:
- Output directory defaults to cache if not provided
        """.trimIndent()
    }
}
