package me.hanslovsky.eksif

import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable

@CommandLine.Command(name = "print-tags")
class PrintExif: Callable<Int> {

    @CommandLine.Parameters(arity = "*", paramLabel = "FILE", description = ["Image files for which to print Exif data"])
    val files = mutableListOf<File>()

    @CommandLine.Option(names = ["--print-file-names"], required = false)
    var printFileNames = false

    @CommandLine.Option(names = ["--help", "-h"], required = false, usageHelp = true)
    var showUsageHelp = false


    override fun call(): Int {
        if (showUsageHelp)
            return 0
        files.forEach { file ->
            if (printFileNames)
                println("${file.path}:")
            file.exifTags.prettyPrint()
        }
        return 0
    }


}