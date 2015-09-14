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

    "Image Width" should {
      "equal 1640" in {
        img.imageWidth must_== Some(1640)
      }
    }
    "Image Length" should {
      "equal 1400" in {
        img.imageLength must_== Some(1400)
      }
    }
    "BitsPerSample" should {
      "equal 8" in {
        img.bitsPerSample must_== Some(8)
      }
    }
    "Compression" should {
      "equal 5" in {
        img.compression must_== Some(5)
      }
    }
    "PhotometricInterpretation" should {
      "equal 2" in {
        img.photometricInterpretation must_== Some(2)
      }
    }
    "FillOrder" should {
      "equal None" in {
        img.fillOrder must_== None
      }
    }
    "SamplesPerPixel" should {
      "equal 3" in {
        img.samplesPerPixel must_== Some(3)
      }
    }
    "RowsPerStrip" should {
      "equal 1" in {
        img.rowsPerStrip must_== Some(1)
      }
    }
    "XResolution" should {
      "equal 300" in {
        img.xResolution must_== Some(300)
      }
    }
    "YResolution" should {
      "equal 300" in {
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

    "Image Width" should {
      "equal 1305" in {
        img.imageWidth must_== Some(1305)
      }
    }
    "Image Length" should {
      "equal 3210" in {
        img.imageLength must_== Some(3210)
      }
    }
    "BitsPerSample" should {
      "equal 8" in {
        img.bitsPerSample must_== Some(8)
      }
    }
    "Compression" should {
      "equal 5" in {
        img.compression must_== Some(5)
      }
    }
    "PhotometricInterpretation" should {
      "equal 5" in {
        img.photometricInterpretation must_== Some(5)
      }
    }
    "FillOrder" should {
      "equal None" in {
        img.fillOrder must_== None
      }
    }
    "SamplesPerPixel" should {
      "equal 4" in {
        img.samplesPerPixel must_== Some(4)
      }
    }
    "RowsPerStrip" should {
      "equal 50" in {
        img.rowsPerStrip must_== Some(50)
      }
    }
    "XResolution" should {
      "equal 300" in {
        img.xResolution must_== Some(300)
      }
    }
    "YResolution" should {
      "equal 300" in {
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

    "Image Width" should {
      "equal 2552" in {
        img.imageWidth must_== Some(2552)
      }
    }
    "Image Length" should {
      "equal 3444" in {
        img.imageLength must_== Some(3444)
      }
    }
    "BitsPerSample" should {
      "equal 1" in {
        img.bitsPerSample must_== Some(1)
      }
    }
    "Compression" should {
      "equal 4" in {
        img.compression must_== Some(4)
      }
    }
    "PhotometricInterpretation" should {
      "equal 0" in {
        img.photometricInterpretation must_== Some(0)
      }
    }
    "FillOrder" should {
      "equal 1" in {
        img.fillOrder must_== Some(1)
      }
    }
    "SamplesPerPixel" should {
      "equal 1" in {
        img.samplesPerPixel must_== Some(1)
      }
    }
    "RowsPerStrip" should {
      "equal 3444" in {
        img.rowsPerStrip must_== Some(3444)
      }
    }
    "XResolution" should {
      "equal 300" in {
        img.xResolution must_== Some(300)
      }
    }
    "YResolution" should {
      "equal 300" in {
        img.yResolution must_== Some(300)
      }
    }
  })
}

/*
/**
 * Specification for Overlaying TIFF Image
 */
class OverlayTIFFImageSpec extends Specification {
  "OverlayTIFFImage Specification".title

  val inputStream = this.getClass.getClassLoader.getResourceAsStream("bilevel.tif")
  val tiff = TIFFImage.fromStream(inputStream).head

}
*/ 