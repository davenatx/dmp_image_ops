package com.dmp

import java.io.File
import com.dmp.image._
import com.dmp.image.ImageOps

import java.awt.image.BufferedImage
import java.awt.Color

import com.sun.media.imageio.plugins.tiff._

import scala.annotation.tailrec

import com.typesafe.scalalogging.LazyLogging

object ToBinary extends App with LazyLogging {

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

  /* omitted binary conversion becuase the issue encountered was curropt metadata */
  def convert(oldFile: File, newFile: File) {
    val img = TIFFImage.fromFile(oldFile)

    val bi = img.head.bi
    val newBi = ImageOps.toBinary(bi)
    val newImg = createTIFFImage(newBi, newBi.getWidth, newBi.getHeight)
    //val newImg = createTIFFImage(bi, bi.getWidth, bi.getHeight)

    TIFFImage.toFile(newFile, List(newImg))
  }

  def processImages(files: List[File]): Unit = {
    @tailrec
    def process(xs: List[File]): Unit = xs match {
      case List() =>
      case head :: tail => {
        logger.info("Processing File: " + head)
        val newFile = new File("C:/Users/dprice/desktop/KF Temp/new/" + head.getName)
        convert(head, newFile)
        process(tail)
      }

    }
    process(files)
  }

  val dir = new File("C:/Users/dprice/desktop/KF Temp/old")
  logger.info("Preparing to Process Files: " + dir.listFiles.toList)
  processImages(dir.listFiles.toList)
}