package com.dmp.image

import java.awt.image.BufferedImage
import java.io.File
import java.awt.Color
import java.awt.image.renderable.ParameterBlock
import java.awt.image.DataBuffer
import java.awt.image.ColorModel
import java.awt.image.ComponentColorModel
import java.awt.image.IndexColorModel
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.RenderingHints

import javax.media.jai.JAI
import javax.media.jai.LookupTableJAI
import javax.media.jai.KernelJAI
import javax.media.jai.ImageLayout
import javax.media.jai.ColorCube

import com.sun.media.imageio.plugins.tiff._

import com.typesafe.scalalogging.LazyLogging
import com.typesafe.config._

/**
 * Overlay an image
 */
object OverlayImage extends LazyLogging {

  /**
   * Create the new image and overlay the existing buffered image on it
   */
  private def createNewImage(bi: BufferedImage, imageWidth: Int, imageLength: Int): BufferedImage = {
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
  private def createTIFFImage(bi: BufferedImage, originalXResolution: Long, originalYResolution: Long): TIFFImage = {
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

/**
 * Convert an RGB Image to Bilevel.
 */
object RGBtoBilevel extends LazyLogging {

  private def toGrayscale(bi: BufferedImage): BufferedImage = {
    val pb = new ParameterBlock
    pb.addSource(bi)
        
    val cm = new ComponentColorModel(
      ColorSpace.getInstance(ColorSpace.CS_GRAY),new Array[Int](8),
      false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE
    )
    
    pb.add(cm)

    /* Create hints with desired ColorModel and derived SampleModel */
    val layout = new ImageLayout();
    layout.setColorModel(cm);
    layout.setSampleModel(
      cm.createCompatibleSampleModel(bi.getWidth,bi.getHeight)
    )
    
    val rh = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout)

    val pi = JAI.create("ColorConvert", pb, rh)
    pi.getAsBufferedImage
  }

  /**
   * Ordered Dither to convert gray image to bi-level.  It's much faster than
   * error diffusion however it does create slightly larger files.
   */
  private def orderedDither(bi: BufferedImage): BufferedImage = {
    /* Color cube for 8 color gray scale image */
    val colorMap = ColorCube.createColorCube(DataBuffer.TYPE_BYTE, 0, new Array[Int](2))

    /* Set the dither mask to the pre-defined 4x4x1 mask */
    val ditherMask = KernelJAI.DITHER_MASK_441

    /* Create a new ParameterBlock */
    val pb = new ParameterBlock()
    pb.addSource(bi).add(colorMap).add(ditherMask)

    val layout = new ImageLayout
    val map = Array[Byte](0x00.toByte, 0xff.toByte)

    val cm = new IndexColorModel(1, 2, map, map, map)
    layout.setColorModel(cm)

    /* Create RenderingHints for the ImageLayout */
    val rh = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout)

    /* Create the ordered dither OpImage */
    val pi = JAI.create("OrderedDither", pb, rh)
    pi.getAsBufferedImage      
  }

  /**
   * Convert an RGB image to Bilevel by converting to grayscale and then performing an
   * ordered dither
   */
  def convert(bi: BufferedImage): BufferedImage = {
    orderedDither(toGrayscale(bi))
  }
}