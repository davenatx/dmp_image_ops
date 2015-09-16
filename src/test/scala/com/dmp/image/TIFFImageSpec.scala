package com.dmp.image

import com.dmp.image._

import org.specs2.mutable._

/**
 * Specification for RGB TIFF Image
 */
class RGBTIFFImageSpec extends Specification {
  "RGBTIFFImage Specification".title

  val inputStream = this.getClass.getClassLoader.getResourceAsStream("rgb.tif")
  TIFFImage.fromStream(inputStream).headOption map (img => {

    "RGB TIFF Image" should {

      "have ImageWidth of 1640" in {
        img.imageWidth must_== Some(1640)
      }

      "have ImageLength of 1400" in {
        img.imageLength must_== Some(1400)
      }

      "have BitsPerSample of 8" in {
        img.bitsPerSample must_== Some(8)
      }

      "have Compression of LZW" in {
        img.compression must_== Some(5)
      }

      "have PhotometricInterpretation of 2" in {
        img.photometricInterpretation must_== Some(2)
      }

      "have FillOrder of None" in {
        img.fillOrder must_== None
      }

      "have SamplesPerPixel of 3" in {
        img.samplesPerPixel must_== Some(3)
      }

      "have RowsPerStrip of 1" in {
        img.rowsPerStrip must_== Some(1)
      }

      "have XResolution of 300" in {
        img.xResolution must_== Some(300)
      }

      "have YResolution of 300" in {
        img.yResolution must_== Some(300)
      }
    }
  })
}

/**
 * Specification for CMYK TIFF Image
 */
class CMYKTIFFImageSpec extends Specification {
  "CMYKTIFFImage Specification".title

  val inputStream = this.getClass.getClassLoader.getResourceAsStream("cmyk.tif")
  TIFFImage.fromStream(inputStream).headOption map (img => {

    "CMYK TIFF Image" should {

      "have ImageWidth of 1305" in {
        img.imageWidth must_== Some(1305)
      }

      "have ImageLength of 3210" in {
        img.imageLength must_== Some(3210)
      }

      "have BitsPerSample of 8" in {
        img.bitsPerSample must_== Some(8)
      }

      "have Compression of LZW" in {
        img.compression must_== Some(5)
      }

      "have PhotometricInterpretation of 5" in {
        img.photometricInterpretation must_== Some(5)
      }

      "have FillOrder of None" in {
        img.fillOrder must_== None
      }

      "have SamplesPerPixel of 4" in {
        img.samplesPerPixel must_== Some(4)
      }

      "have RowsPerStrip of 50" in {
        img.rowsPerStrip must_== Some(50)
      }

      "have XResolution of 300" in {
        img.xResolution must_== Some(300)
      }

      "have YResolution of 300" in {
        img.yResolution must_== Some(300)
      }
    }
  })
}

/**
 * Specification for BILEVEL_TIFFImage
 */
class BiLevelTIFFImageSpec extends Specification {
  "BiLevelTIFFImage Specification".title

  val inputStream = this.getClass.getClassLoader.getResourceAsStream("bilevel.tif")
  TIFFImage.fromStream(inputStream).headOption map (img => {

    "BiLevel TIFF Image" should {

      "have ImageWidth of 2552" in {
        img.imageWidth must_== Some(2552)
      }

      "have ImageLength of 3444" in {
        img.imageLength must_== Some(3444)
      }

      "have BitsPerSample of 1" in {
        img.bitsPerSample must_== Some(1)
      }

      "have Compression of CCITT T.6" in {
        img.compression must_== Some(4)
      }

      "have PhotometricInterpretation of 0" in {
        img.photometricInterpretation must_== Some(0)
      }

      "have FillOrder of 1" in {
        img.fillOrder must_== Some(1)
      }

      "have SamplesPerPixel of 1" in {
        img.samplesPerPixel must_== Some(1)
      }

      "have RowsPerStrip of 3444" in {
        img.rowsPerStrip must_== Some(3444)
      }

      "have XResolution of 300" in {
        img.xResolution must_== Some(300)
      }

      "have YResolution of 300" in {
        img.yResolution must_== Some(300)
      }
    }
  })
}

import java.io.File
import java.nio.file.Files
/**
 * Specification for Overlaying TIFF Image
 */
class OverlayTIFFImageSpec extends Specification {
  "OverlayTIFFImage Specification".title

  val inputStream = this.getClass.getClassLoader.getResourceAsStream("overlay.tif")
  /* Temporary file to write overlayed image to */
  val tempFile = File.createTempFile("dmp_image_ops", ".tif")
  TIFFImage.fromStream(inputStream).headOption map (img => {

    "Image to Overlay" should {

      "have ImageWidth of 3568" in {
        img.imageWidth must_== Some(3568)
      }

      "have ImageLenth of 1513" in {
        img.imageLength must_== Some(1513)
      }

      "have BitsPerSample of 1" in {
        img.bitsPerSample must_== Some(1)
      }

      "have Compression of CCITT T.6" in {
        img.compression must_== Some(4)
      }

      "have PhotometricInterpretation of 0" in {
        img.photometricInterpretation must_== Some(0)
      }

      "have FillOrder of 1" in {
        img.fillOrder must_== Some(1)
      }

      "have SamplesPerPixel of 1" in {
        img.samplesPerPixel must_== Some(1)
      }

      "have RowsPerStrip of 3568" in {
        img.rowsPerStrip must_== Some(3568)
      }

      "have XResolution of 300" in {
        img.xResolution must_== Some(300)
      }

      "have YResolution of 300" in {
        img.yResolution must_== Some(300)
      }
    }
    val newImage = OverlayImage.overlay(img, 3568, 5536)
    TIFFImage.toFile(tempFile, List(newImage))

  })

  inputStream.close

  val newInputStream = Files.newInputStream(tempFile.toPath)

  TIFFImage.fromStream(newInputStream).headOption map (img => {
    "Overlayed image" should {

      "have ImageWidth of 3568" in {
        img.imageWidth must_== Some(3568)
      }

      "have ImageLength of 5536" in {
        img.imageLength must_== Some(5536)
      }

      "have BitsPerSample of None" in {
        img.bitsPerSample must_== None
      }

      "have Compression of CCITT T.6" in {
        img.compression must_== Some(4)
      }

      "have PhotometricInterpretation of 1" in {
        img.photometricInterpretation must_== Some(1)
      }

      "have FillOrder of None" in {
        img.fillOrder must_== None
      }

      "have SamplesPerPixel of 1" in {
        img.samplesPerPixel must_== Some(1)
      }

      "have RowsPerStrip of 18" in {
        img.rowsPerStrip must_== Some(18)
      }

      "have XResolution of 300" in {
        img.xResolution must_== Some(300)
      }

      "have YResolution of 300" in {
        img.yResolution must_== Some(300)
      }

      "have deleted temp file" in {
        newInputStream.close
        tempFile.delete must_== true
      }
    }
  })
}