package com.dmp.image

import java.awt.image.BufferedImage
import java.io.File
import java.awt.Color

import com.typesafe.scalalogging.LazyLogging
import com.typesafe.config._

import com.sun.media.imageio.plugins.tiff._

object OverlayImage extends LazyLogging {

  /**
   * Create the new image and overlay the existing buffered image on it
   */
  def createNewImage(bi: BufferedImage, imageWidth: Int, imageLength: Int): BufferedImage = {
    val newBi = new BufferedImage(imageWidth, imageLength, overlayImageType)
    val g = newBi.getGraphics
    g.setColor(Color.white)
    g.fillRect(0, 0, imageWidth, imageLength)
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
    val resolutionUnitFunc = TIFFImage.addResolutionUnitTag _
    // Compose functions
    val ifdTransformFunc = softwareTagFunc compose xResolutionFunc compose yResolutionFunc compose resolutionUnitFunc

    new TIFFImage(ifdTransformFunc(TIFFImage.createIfd(bi)), bi)
  }

  /**
   * Overlay the old image on a new image.
   */
  def overlay(image: TIFFImage, imageWidth: Int, imageLength: Int): TIFFImage = {
    val bi = createNewImage(image.bi, imageWidth, imageLength)
    createTIFFImage(bi, image.xResolution.getOrElse(defaultXResolution), image.yResolution.getOrElse(defaultYResolution))
  }
}