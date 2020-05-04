package me.hanslovsky.eksif

import com.drew.metadata.Tag
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

    @CommandLine.Option(names = ["--help", "-h"], required = false, usageHelp = true, description = ["Print this message."])
    var showUsageHelp = false

    @CommandLine.Option(
            names = ["--exclude"],
            required = false,
            paramLabel = "PATTERN",
            converter = [RegexConverter::class],
            description = [
                "Exclude any tags that match this regular expression. " +
                "Can be specified multiple times. Will be overriden by --include"])
    val excludes = mutableListOf<Regex>()

    @CommandLine.Option(
            names = ["--include"],
            required = false,
            paramLabel = "PATTERN",
            converter = [RegexConverter::class],
            description = [
                "Include any tags that match this regular expression, even if matched by --exclude. " +
                "Can be specified multiple times."])
    val includes = mutableListOf<Regex>()

    private val tagFilter = { tag: Tag ->
        !excludes.any { it.containsMatchIn(tag.tagName) } || includes.any { it.containsMatchIn(tag.tagName) }
    }


    override fun call(): Int {
        if (showUsageHelp)
            return 0
        files.forEach { file ->
            if (printFileNames)
                println("${file.path}:")
            file.exifTags.prettyPrint(printTableHeader = printTableHeaders, tagFilter = tagFilter)
        }
        return 0
    }


}

private fun Collection<Tag>.prettyPrint(
        printTableHeader: Boolean,
        tagFilter: (Tag) -> Boolean = { true },
        printer: (String) -> Unit = { println(it) }) {
    val maxTagNameLength = map { it.tagName.length }.max() ?: "Tag".length
    val maxDescriptionLength = map { it.description.length }.max() ?: "Description".length
    if (printTableHeader) {
        printer("${"Tag".padEnd(maxTagNameLength)} Description")
        printer("".padEnd(maxTagNameLength + maxDescriptionLength + 1, '-'))
    }
    filter(tagFilter).forEach { printer("${it.tagName.padEnd(maxTagNameLength)} ${it.description}") }
}