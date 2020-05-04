/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package me.hanslovsky.eksif

import picocli.AutoComplete
import picocli.CommandLine
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@CommandLine.Command(
        name = "eksif",
        subcommands = [PrintExif::class, Rename::class, AutoComplete.GenerateCompletion::class])
class Eksif: Callable<Int> {

    @CommandLine.Option(names = ["--help", "-h"], required = false, usageHelp = true, description = ["Print this message."])
    var showUsageHelp = false

    override fun call(): Int = 0

    companion object {
        @JvmStatic
        fun main(args: Array<String>): Unit = exitProcess(CommandLine(Eksif()).execute(*args))
    }
}
