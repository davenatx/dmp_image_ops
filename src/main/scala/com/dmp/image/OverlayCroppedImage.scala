package com.dmp.image

import java.awt.image.BufferedImage
import java.io.File
import java.awt.Color

import com.typesafe.scalalogging.LazyLogging
import com.typesafe.config._

object OverlayImage extends LazyLogging {

  // Ultimatly load these from properties	
  val IMAGE_WIDTH = 3568
  val IMAGE_LENGTH = 5536
  val IMAGE_TYPE = BufferedImage.TYPE_BYTE_BINARY
  val DEFAULT_X_RESOLUTION = 200
  val DEFAULT_Y_RESOLUTION = 200

  def apply(f: File) {
    logger.info("Processing: " + f.getName)
    overlayImage(f)
  }

  def createNewImage(bi: BufferedImage): BufferedImage = {
    val newBi = new BufferedImage(IMAGE_WIDTH, IMAGE_LENGTH, IMAGE_TYPE)
    val g = newBi.getGraphics
    g.setColor(Color.white)
    g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_LENGTH)
    g.drawImage(bi, 0, 0, null)
    g.dispose
    newBi
  }

  /**
   * Create new TIFFImage from BufferedImage using the original images resolution
   */
  def createTIFFImage(bi: BufferedImage, originalXResolution: Long, originalYResolution: Long): TIFFImage = {
    val ifd = TIFFImage.createIfd(bi)
    TIFFImage.addSoftwareTag(ifd)
    TIFFImage.addXResolutionTag(ifd, originalXResolution)
    TIFFImage.addYResolutionTag(ifd, originalYResolution)
    new TIFFImage(ifd, bi)
  }

  /**
   * Overlay the old image on a new image.  The assumption is made we are working with single page TIFF files
   */
  def overlayImage(f: File) {
    val image = TIFFImage.fromFile(f)(0)
    val bi = createNewImage(image.bi)
    val newImage = createTIFFImage(bi, image.xResolution.getOrElse(DEFAULT_X_RESOLUTION), image.yResolution.getOrElse(DEFAULT_Y_RESOLUTION))
    val newFile = new File("sample_images/new" + f.getName + ".TIF")
    TIFFImage.toFile(newFile, List(newImage))
  }
}

object Overlay extends App {
  OverlayImage(new File("sample_images/62135.001"))
}