package kscript.app

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.startsWith
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.Charset

internal class KscriptKtTest {
    @Test
    fun test_main() {
        val args = arrayOf(
//            "--package",
            "examples/httpClient.kts"
        )

        val (command, ex) = runScript(args)
        assertThat(ex).isNull()
        assertThat(command).startsWith("java")

        val output = runCommand(command)
        assertThat(output).isEqualTo("418| 418 I'm a teapot\n")
    }

    private fun runScript(args: Array<String>) = ByteArrayOutputStream().use { baos ->
        val out = System.out
        System.setOut(PrintStream(baos))
        val e = kotlin.runCatching { main(args) }
        System.setOut(out)
        baos.toString(Charset.defaultCharset()) to e.exceptionOrNull()
    }

    private fun runCommand(cmd: String): String {
        val process = ProcessBuilder().command(cmd.split(Regex("\\s+"))).start()
        process.waitFor()
        return process.inputStream.reader().readText()
    }
}
