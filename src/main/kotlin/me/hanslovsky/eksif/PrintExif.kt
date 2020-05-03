package me.hanslovsky.eksif

import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable

@CommandLine.Command(name = "print-tags")
class PrintExif: Callable<Int> {

    @CommandLine.Parameters(arity = "*", paramLabel = "FILE", description = ["Image files for which to print Exif data"])
    val files = mutableListOf<File>()

    @CommandLine.Option(names = ["--no-print-filenames"], required = false, negatable = true)
    var printFileNames = true

    @CommandLine.Option(names = ["--no-print-table-headers"], required = false, negatable = true)
    var printTableHeaders = true

    @CommandLine.Option(names = ["--help", "-h"], required = false, usageHelp = true)
    var showUsageHelp = false


    override fun call(): Int {
        if (showUsageHelp)
            return 0
        files.forEach { file ->
            if (printFileNames)
                println("${file.path}:")
            file.exifTags.prettyPrint(printTableHeader = printTableHeaders)
        }
        return 0
    }


}