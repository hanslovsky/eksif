package me.hanslovsky.eksif

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import com.drew.metadata.StringValue
import com.drew.metadata.Tag
import com.drew.metadata.exif.ExifDirectoryBase
import com.drew.metadata.exif.ExifIFD0Directory
import io.mockk.every
import io.mockk.mockkStatic
import java.io.File
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.test.Test

internal class PrintExifTest {

    @Test
    fun `test pretty print exif`() {
        val file = File("")
        val directory = ExifIFD0Directory().apply {
            setStringValue(ExifDirectoryBase.TAG_MAKE, "F1".stringValue)
            setStringValue(ExifDirectoryBase.TAG_MODEL, "F2".stringValue)
            setStringValue(ExifDirectoryBase.TAG_DATETIME, SimpleDateFormat("yyyy:MM:dd HH:mm:ss").format(now).stringValue)
        }
        mockkStatic(ImageMetadataReader::class)
        every { ImageMetadataReader.readMetadata(file) } returns Metadata().apply { addDirectory(directory) }

        with (PrintExif) {
            file.exifTags.prettyPrint(true)
        }
        
    }

}

private val now = Date()
private val String.stringValue get() = StringValue(toByteArray(Charset.defaultCharset()), Charset.defaultCharset())