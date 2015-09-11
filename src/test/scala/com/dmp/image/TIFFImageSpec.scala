package com.dmp.image

import com.dmp.image._

import org.specs2.mutable._

/**
 * Specification for TIFFImage
 */
class TIFFImageSpec extends Specification {
  "TIFFImage Specification".title
  sequential

  val inputStream = this.getClass.getClassLoader.getResourceAsStream("rgb.tif")
  val tiff = TIFFImage.fromStream(inputStream).head

  "Image Width" should {
    "equal 1640" in {
      tiff.imageWidth must_== Some(1640)
    }
  }
}