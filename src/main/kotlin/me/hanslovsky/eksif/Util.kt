package me.hanslovsky.eksif

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Tag
import com.drew.metadata.exif.ExifIFD0Directory
import java.io.File

val File.metaData get() = ImageMetadataReader.readMetadata(this)
val File.exifIFD0Directories get() = metaData.getDirectoriesOfType(ExifIFD0Directory::class.java)
val File.exifTags get() = exifIFD0Directories.flatMap { it.tags }

fun Collection<Tag>.prettyPrint(printer: (String) -> Unit = { println(it) }) {
    val maxTagNameLength = map { it.tagName.length }.max() ?: "Tag".length
    val maxDescriptionLength = map { it.description.length }.max() ?: "Description".length
    printer("${"Tag".padEnd(maxTagNameLength)} Description")
    printer("".padEnd(maxTagNameLength + maxDescriptionLength + 1, '-'))
    forEach { printer("${it.tagName.padEnd(maxTagNameLength)} ${it.description}") }
}

val String.file get() = File(this)