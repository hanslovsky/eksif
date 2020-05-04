package me.hanslovsky.eksif

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import java.io.File

val File.metaData get() = ImageMetadataReader.readMetadata(this)
val File.exifIFD0Directories get() = metaData.getDirectoriesOfType(ExifIFD0Directory::class.java)
val File.exifTags get() = exifIFD0Directories.flatMap { it.tags }

val String.file get() = File(this)