package com.dmp.image

import java.awt.image.BufferedImage
import java.io.File
import java.awt.Color

import com.typesafe.scalalogging.LazyLogging
import com.typesafe.config._

import com.sun.media.imageio.plugins.tiff._

object OverlayImage extends LazyLogging {

  // Load these from properties	
  val IMAGE_WIDTH = 3568
  val IMAGE_LENGTH = 5536
  val IMAGE_TYPE = BufferedImage.TYPE_BYTE_BINARY
  val DEFAULT_X_RESOLUTION = 200
  val DEFAULT_Y_RESOLUTION = 200

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
    val softwareTagFunc = TIFFImage.addSoftwareTag _
    // partially applied functions that take TIFFDirectory
    val xResolutionFunc = TIFFImage.addXResolutionTag(_: TIFFDirectory, originalXResolution)
    val yResolutionFunc = TIFFImage.addYResolutionTag(_: TIFFDirectory, originalYResolution)
    // Compose functions
    val ifdTransformFunc = softwareTagFunc compose xResolutionFunc compose yResolutionFunc

    new TIFFImage(ifdTransformFunc(TIFFImage.createIfd(bi)), bi)
  }

  /**
   * Overlay the old image on a new image.
   */
  def overlay(image: TIFFImage): TIFFImage = {
    val bi = createNewImage(image.bi)
    createTIFFImage(bi, image.xResolution.getOrElse(DEFAULT_X_RESOLUTION), image.yResolution.getOrElse(DEFAULT_Y_RESOLUTION))
  }
}

object Overlay extends App with LazyLogging {
  val image = TIFFImage.fromFile(new File("sample_images/62135.001"))
  logger.info("Current Image: " + image.head)
  val newImage = OverlayImage.overlay(image.head)
  logger.info("New Image:" + newImage)
  TIFFImage.toFile(new File("sample_images/new-image.TIF"), List(newImage))
}