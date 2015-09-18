package com.dmp.image

import scala.annotation.tailrec
import scala.util.{ Try, Success, Failure }
import scala.util.control.Exception._

import javax.imageio._
import java.io.{ Closeable, File, InputStream, OutputStream }
import java.nio.file.{ Files, Path }
import java.awt.image.BufferedImage
import java.util.Locale

import com.sun.media.imageioimpl.plugins.tiff._
import com.sun.media.imageio.plugins.tiff._

import com.typesafe.scalalogging.LazyLogging
import com.typesafe.config._

/**
 * Read and write both single and mutltipage TIFF images
 */
object TIFFImage extends LazyLogging {

  def fromFile(file: File): List[TIFFImage] = fromPath(file.toPath)

  def fromPath(path: Path): List[TIFFImage] = fromStream(Files.newInputStream(path))

  def fromStream(is: InputStream): List[TIFFImage] = {
    Try(read(is)) match {
      case Failure(thrown) => {
        logger.warn("Failed reading from stream: " + thrown)
        Nil
      }
      case Success(s) => s
    }
  }

  def toFile(file: File, images: List[TIFFImage]): Boolean = toPath(file.toPath, images)

  def toPath(path: Path, images: List[TIFFImage]): Boolean = toStream(Files.newOutputStream(path), images)

  def toStream(os: OutputStream, images: List[TIFFImage]): Boolean = {
    Try(write(os, images)) match {
      case Failure(thrown) => {
        logger.warn("Failed writing to stream: " + thrown)
        false
      }
      case Success(s) => true
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
      TIFFTag.TIFF_ASCII, 1, Array(softwareTag))
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

  /**
   * Add the Resolution Unit tag to the IFD
   *
   * The value is loaded form the properties file
   */
  def addResolutionUnitTag(ifd: TIFFDirectory): TIFFDirectory = {
    val newIfd = ifd.clone.asInstanceOf[TIFFDirectory]
    val resolutionUnitField = new TIFFField(BaselineTIFFTagSet.getInstance.getTag(BaselineTIFFTagSet.TAG_RESOLUTION_UNIT),
      TIFFTag.TIFF_SHORT, 1, Array[Char](resolutionUnit.asInstanceOf[Char]))
    newIfd.addTIFFField(resolutionUnitField)
    newIfd
  }

  /**
   * Automatically close resources
   */
  private def withCloseable[T <: Closeable, R](t: T)(f: T => R): R = {
    allCatch.andFinally { t.close } apply { f(t) }
  }

  /**
   * Read TIFF image(s)
   */
  private def read(is: InputStream): List[TIFFImage] = {
    withCloseable(ImageIO.createImageInputStream(is)) { iis =>
      val reader = new TIFFImageReader(new TIFFImageReaderSpi())
      reader.setInput(iis, false, false)
      readImages(reader, reader.getNumImages(true))
    }
  }

  /**
   * Write TIFF image(s)
   */
  private def write(os: OutputStream, images: List[TIFFImage]) {
    withCloseable(ImageIO.createImageOutputStream(os)) { ios =>
      val writer = new TIFFImageWriter(new TIFFImageWriterSpi())
      val writeParam = new TIFFImageWriteParam(Locale.getDefault)
      writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT)
      writeParam.setCompressionType(compressionType)
      writer.setOutput(ios)
      writer.prepareWriteSequence(new TIFFStreamMetadata)
      images map (x => writer.writeToSequence(new IIOImage(x.bi, null, x.ifd.getAsMetadata), writeParam))
      writer.endWriteSequence
    }
  }

  /**
   * Helper to recurisvily read all images in TIFF
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
      logger.debug("Failed reading long TIFF Tag: " + thrown)
      None
    }
    case Success(s) => Some(s)
  }

  /**
   * Retrieve TIFF Tag value as int from TIFField
   */
  private def TIFFTagIntValue(tiffField: Try[TIFFField]): Option[Int] = tiffField map (_.getAsInt(0)) match {
    case Failure(thrown) => {
      logger.debug("Failed reading int TIFF Tag: " + thrown)
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

  def stripOffsets: Option[Long] = getLongTIFFTagValue(ifd, BaselineTIFFTagSet.TAG_STRIP_OFFSETS)

  def samplesPerPixel: Option[Int] = getIntTIFFTagValue(ifd, BaselineTIFFTagSet.TAG_SAMPLES_PER_PIXEL)

  def rowsPerStrip: Option[Long] = getLongTIFFTagValue(ifd, BaselineTIFFTagSet.TAG_ROWS_PER_STRIP)

  def stripByteCounts: Option[Long] = getLongTIFFTagValue(ifd, BaselineTIFFTagSet.TAG_STRIP_BYTE_COUNTS)

  def xResolution: Option[Long] = getLongTIFFTagValue(ifd, BaselineTIFFTagSet.TAG_X_RESOLUTION)

  def yResolution: Option[Long] = getLongTIFFTagValue(ifd, BaselineTIFFTagSet.TAG_Y_RESOLUTION)

  override def toString() = {
    "ImageWidth: " + imageWidth.getOrElse("") + ", ImageLength: " + imageLength.getOrElse("") +
      ", BitsPerSample: " + bitsPerSample.getOrElse("") + ", Compression: " + compression.getOrElse("") +
      ", PhotometricInterpretation: " + photometricInterpretation.getOrElse("") + ", FillOrder: " + fillOrder.getOrElse("") +
      ", StripOffsets: " + stripOffsets.getOrElse("") + ", SamplesPerPixel: " + samplesPerPixel.getOrElse("") +
      ", RowsPerStrip: " + rowsPerStrip.getOrElse("") + ", StripByteCounts: " + stripByteCounts.getOrElse("") +
      ", XResolution: " + xResolution.getOrElse("") + ", YResolution: " + yResolution.getOrElse("")

  }
}