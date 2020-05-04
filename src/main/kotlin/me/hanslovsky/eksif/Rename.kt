package me.hanslovsky.eksif

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import com.drew.metadata.exif.ExifDirectoryBase
import com.drew.metadata.exif.ExifIFD0Directory
import picocli.CommandLine
import java.io.File
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat
import java.util.concurrent.Callable

@CommandLine.Command(name = "rename", description = ["Read date from EXIF data and add as prefix to file name."])
class Rename: Callable<Int> {

    enum class ExifTag(val tag: Int) {
        DATETIME(ExifDirectoryBase.TAG_DATETIME),
        DATETIME_ORIGINAL(ExifDirectoryBase.TAG_DATETIME_ORIGINAL),
        DATETIME_DIGITIZED(ExifDirectoryBase.TAG_DATETIME_DIGITIZED)
    }

    @CommandLine.Option(names = ["--dry-run", "-d"], required = false, description = ["Do not do anything. Use in conjunction with the `--verbose' flag to print what would happen."])
    private var dryRun: Boolean = false

    @CommandLine.Parameters(arity = "*", description = ["List of image files to be renamed"])
    private var files: MutableList<File> = mutableListOf()

    @CommandLine.Option(names = ["--exif-tag-type", "-t"], required = false, defaultValue = "DATETIME")
    private lateinit var tagType: ExifTag

    @CommandLine.Option(names = ["--format", "-f"], required = false, defaultValue = "yyyyMMdd_HHmmss_", description=["Date will be formatted and prefixed to filename."])
    private lateinit var outputFormatString: String

    @CommandLine.Option(names = ["--overwrite-existing", "-o"], required = false, description = ["Overwrite existing target files. Will fail if not specified and target files exist."])
    private var overwriteExisting = false

    @CommandLine.Option(names = ["--verbose"], required = false)
    private var verbose = false

    @CommandLine.Option(names = ["--keep-original", "-k"], required = false, description = ["Copy instead of moving (keep original)."])
    private var copy = false

    override fun call(): Int {
        return try {
            rename()
            0
        } catch (e: Exception) {
            System.err.print(e.message)
            1
        }
    }

    private fun rename() {
        val filesToMeta = mutableMapOf<File, Metadata>()

        val filesWithoutMetaData = mutableMapOf<File, Exception>()

        val uniqueFiles: List<File> = files.filter { file: File ->
            if (file in filesToMeta) false
            else {
                try {
                    filesToMeta[file] = ImageMetadataReader.readMetadata(file)
                    true
                } catch(exception: Exception) {
                    filesWithoutMetaData[file] = exception
                    false
                }
            }
        }

        val filesToDateTime = filesToMeta.mapValues {
            try {
                it
                        .value
                        .getDirectoriesOfType(ExifIFD0Directory::class.java)
                        .flatMap { it.tags }
                        .filter { it.tagType == tagType.tag }
                        .also { if (it.isEmpty()) throw Exception("Tag `${tagType}' not found.") }
                        .first()
                        .description
                        .let { tagDateFormat.parse(it) }
            } catch(exception: Exception) {
                filesWithoutMetaData[it.key] = exception
            }
        }

        filesWithoutMetaData.takeIf { it.isNotEmpty() }?.let {
            throw Exception("Unable to extract meta data from files:\n${it.entries.map{ "`${it.key}': ${it.value.message}" }.joinToString("\n")}")
        }

        val outputDateFormat = SimpleDateFormat(outputFormatString)

        val filesToNewFiles = filesToDateTime.mapValues { it.key.prefixName(outputDateFormat.format(it.value)) }

        filesToNewFiles.values.filter { it.exists() }.takeIf { it.isNotEmpty() && !overwriteExisting }?.let {
            throw Exception("Output files already exist:\n${it.joinToString("\n")}\nUse the `--overwrite-existing / -o' flag to force overwrite.")
        }

        val copyOptions = if (overwriteExisting) arrayOf(StandardCopyOption.REPLACE_EXISTING) else arrayOf<CopyOption>()

        uniqueFiles.forEach { file ->
            if (verbose)
                println("`$file' --${if (copy) "copy" else "move"}--> `${filesToNewFiles[file]}'")

            if (!dryRun) {
                if (copy) {
                    Files.copy(file.toPath(), filesToNewFiles[file]!!.toPath(), *copyOptions)
                } else
                    Files.move(file.toPath(), filesToNewFiles[file]!!.toPath(), *copyOptions)
            }
        }
    }

    companion object {
        private val tagDateFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss")
        private fun File.prefixName(prefix: String) = updateName { "$prefix$it" }
        private fun File.updateName(nameUpdate: (String) -> String) = File(parent, nameUpdate(name))
    }
}
