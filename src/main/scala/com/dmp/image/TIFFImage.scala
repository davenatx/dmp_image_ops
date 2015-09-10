package com.dmp.image

import scala.annotation.tailrec
import scala.util.{ Try, Success, Failure }

import javax.imageio._
import java.io.{ File, InputStream }
import java.nio.file.{ Files, Path }
import java.awt.image.BufferedImage
import java.util.Locale

import com.sun.media.imageioimpl.plugins.tiff._
import com.sun.media.imageio.plugins.tiff._

import com.typesafe.scalalogging.LazyLogging
import com.typesafe.config._

/**
 * Read both single and mutltipage TIFF files
 *
 * ToDo - Add mthods to load the TIFF from an InputStream in addition to a file
 */
object TIFFImage extends LazyLogging {

  val SOFTWARE_TAG = "DMP_IMAGE_OPS"

  def apply(file: File): List[TIFFImage] = fromFile(file)

  /**
   *  Try catch is used to handle exceptions thrown by Java api.  Also, finally properly closes resouces.
   */
  def fromFile(file: File): List[TIFFImage] = {
    try {
      val iis = ImageIO.createImageInputStream(Files.newInputStream(file.toPath))
      val reader = new TIFFImageReader(new TIFFImageReaderSpi())

      try {
        reader.setInput(iis, false, false)

        readImages(reader, reader.getNumImages(true))

      } catch {
        case e: Exception => logger.warn(e.getMessage); Nil
      } finally {
        reader.dispose
        iis.close
      }

    } catch {
      case e: Exception => logger.warn(e.getMessage); Nil
    }
  }

  /**
   * Helper method to recurisvily read all images in TIFF
   */
  private def readImages(reader: TIFFImageReader, totalImages: Int): List[TIFFImage] = {
    @tailrec
    def read(currentImage: Int, accu: List[TIFFImage]): List[TIFFImage] = currentImage < totalImages match {
      case true => {
        val ifd = TIFFDirectory.createFromMetadata(reader.getImageMetadata(currentImage))
        val bi = reader.read(0, reader.getDefaultReadParam)
        read(currentImage + 1, new TIFFImage(ifd, bi) :: accu)
      }
      case false => {
        accu
      }
    }

    read(0, Nil).reverse
  }

  /**
   *  Try catch is used to handle exceptions thrown by Java api.  Also, finally properly closes resouces.
   */
  def toFile(file: File, images: List[TIFFImage]): Boolean = {
    try {
      val ios = ImageIO.createImageOutputStream(Files.newOutputStream(file.toPath))
      val writer = new TIFFImageWriter(new TIFFImageWriterSpi())
      val writeParam = new TIFFImageWriteParam(Locale.getDefault)
      writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT)
      writeParam.setCompressionType(new TIFFT6Compressor().getCompressionType)

      try {
        writer.setOutput(ios)
        writer.prepareWriteSequence(new TIFFStreamMetadata)
        images map (x => writer.writeToSequence(new IIOImage(x.bi, null, x.ifd.getAsMetadata), writeParam))
        writer.endWriteSequence
        ios.flush
        true

      } catch {
        case e: Exception => logger.warn(e.getMessage); false
      } finally {
        writer.dispose
        ios.close
      }

    } catch {
      case e: Exception => logger.warn(e.getMessage); false
    }
  }

  /**
   * Helper mehtod to retrieve Long TIFF Tag Value
   */
  def getLongTIFFTagValue(tiffDirectory: TIFFDirectory, tag: Int): Option[Long] = {
    TIFFTagLongValue(TIFFFieldValue(tiffDirectory, tag))
  }

  /**
   * Helper method to retrieve Int TIFF Tag Value
   */
  def getIntTIFFTagValue(tiffDirectory: TIFFDirectory, tag: Int): Option[Int] = {
    TIFFTagIntValue(TIFFFieldValue(tiffDirectory, tag))
  }
  /**
   * Read TIFF Field
   *
   * Try is used becuase there is no garantee the tag exists
   */
  private def TIFFFieldValue(tiffDirectory: TIFFDirectory, tag: Int): Try[TIFFField] = {

    def TIFFField(ifd: TIFFDirectory, tag: Int) = ifd.getTIFFField(tag)

    for {
      tagValue <- Try(TIFFField(tiffDirectory, tag))
    } yield tagValue
  }

  /**
   * Retrieve TIFF Tag value as long from TIFField
   */
  private def TIFFTagLongValue(tiffField: Try[TIFFField]): Option[Long] = tiffField map (_.getAsLong(0)) match {
    case Failure(thrown) => {
      logger.warn("Failure: " + thrown)
      None
    }
    case Success(s) => Some(s)
  }

  /**
   * Retrieve TIFF Tag value as int from TIFField
   */
  private def TIFFTagIntValue(tiffField: Try[TIFFField]): Option[Int] = tiffField map (_.getAsInt(0)) match {
    case Failure(thrown) => {
      logger.warn("Failure: " + thrown)
      None
    }
    case Success(s) => Some(s)
  }

  /**
   * Multi dimensional array representing the resolution TIFF tag
   */
  private def createResolutionArray(resolution: Long): Array[Array[Long]] = {
    val array = Array.ofDim[Long](1, 2)
    array(0)(0) = resolution
    array(0)(1) = 1
    array
  }

  /**
   * Create TIFFDirectory (ifd) for Buffered Image
   */
  def createIfd(bi: BufferedImage): TIFFDirectory = {
    val writer = new TIFFImageWriter(new TIFFImageWriterSpi())
    val writeParam = new TIFFImageWriteParam(Locale.getDefault)
    val its = new ImageTypeSpecifier(bi.getColorModel, bi.getSampleModel)
    val md = writer.getDefaultImageMetadata(its, writeParam)
    TIFFDirectory.createFromMetadata(md)
  }

  /**
   * Add the software tag to the IFD
   */
  def addSoftwareTag(ifd: TIFFDirectory): TIFFDirectory = {
    val newIfd = ifd.clone.asInstanceOf[TIFFDirectory]
    val softwareField = new TIFFField(BaselineTIFFTagSet.getInstance.getTag(BaselineTIFFTagSet.TAG_SOFTWARE),
      TIFFTag.TIFF_ASCII, 1, Array(SOFTWARE_TAG))
    newIfd.addTIFFField(softwareField)
    newIfd
  }

  /**
   * Add the X Resolution tag to the IFD
   */
  def addXResolutionTag(ifd: TIFFDirectory, resolution: Long): TIFFDirectory = {
    val newIfd = ifd.clone.asInstanceOf[TIFFDirectory]
    val xResolutionField = new TIFFField(BaselineTIFFTagSet.getInstance.getTag(BaselineTIFFTagSet.TAG_X_RESOLUTION),
      TIFFTag.TIFF_RATIONAL, 1, createResolutionArray(resolution))
    newIfd.addTIFFField(xResolutionField)
    newIfd
  }

  /**
   * Add the Y Resolution tag to the IFD
   */
  def addYResolutionTag(ifd: TIFFDirectory, resolution: Long): TIFFDirectory = {
    val newIfd = ifd.clone.asInstanceOf[TIFFDirectory]
    val yResolutionField = new TIFFField(BaselineTIFFTagSet.getInstance.getTag(BaselineTIFFTagSet.TAG_Y_RESOLUTION),
      TIFFTag.TIFF_RATIONAL, 1, createResolutionArray(resolution))
    newIfd.addTIFFField(yResolutionField)
    newIfd
  }

}

/**
 * Representation of TIFF Image
 *
 * ToDO - Add additional tag methods
 */
import TIFFImage._

case class TIFFImage(ifd: TIFFDirectory, bi: BufferedImage) extends LazyLogging {

  def imageWidth: Option[Long] = getLongTIFFTagValue(ifd, BaselineTIFFTagSet.TAG_IMAGE_WIDTH)

  def imageLength: Option[Long] = getLongTIFFTagValue(ifd, BaselineTIFFTagSet.TAG_IMAGE_LENGTH)

  def bitsPerSample: Option[Int] = getIntTIFFTagValue(ifd, BaselineTIFFTagSet.TAG_BITS_PER_SAMPLE)

  def compression: Option[Int] = getIntTIFFTagValue(ifd, BaselineTIFFTagSet.TAG_COMPRESSION)

  def photometricInterpretation: Option[Int] = getIntTIFFTagValue(ifd, BaselineTIFFTagSet.TAG_PHOTOMETRIC_INTERPRETATION)

  def fillOrder: Option[Int] = getIntTIFFTagValue(ifd, BaselineTIFFTagSet.TAG_FILL_ORDER)

  def samplesPerPixel: Option[Int] = getIntTIFFTagValue(ifd, BaselineTIFFTagSet.TAG_SAMPLES_PER_PIXEL)

  def rowsPerStrip: Option[Long] = getLongTIFFTagValue(ifd, BaselineTIFFTagSet.TAG_ROWS_PER_STRIP)

  def xResolution: Option[Long] = getLongTIFFTagValue(ifd, BaselineTIFFTagSet.TAG_X_RESOLUTION)

  def yResolution: Option[Long] = getLongTIFFTagValue(ifd, BaselineTIFFTagSet.TAG_Y_RESOLUTION)

  override def toString() = {
    "ImageWidth: " + imageWidth.getOrElse("") + ", ImageLength: " + imageLength.getOrElse("") +
      ", BitsPerSample: " + bitsPerSample.getOrElse("") + ", Compression: " + compression.getOrElse("") +
      ", PhotometricInterpretation: " + photometricInterpretation.getOrElse("") + ", FillOrder: " + fillOrder.getOrElse("") +
      ", SamplesPerPixel: " + samplesPerPixel.getOrElse("") + ", RowsPerStrip: " + rowsPerStrip.getOrElse("") +
      ", XResolution: " + xResolution.getOrElse("") + ", YResolution: " + yResolution.getOrElse("")
  }
}