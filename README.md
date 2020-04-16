# dmp_image_ops
TIFF Image Operations

Work in process

## Proof of concept to convert Color to Binary
```
import java.io.File
import com.dmp.image._
import com.dmp.image.ImageOps

import java.awt.image.BufferedImage
import java.awt.Color

import com.sun.media.imageio.plugins.tiff._

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

val f = new File("C:/Users/dprice/Desktop/ME Test/2020002863.014.tif")

val img = TIFFImage.fromFile(f)

val bi = img.head.bi

val newBi = ImageOps.toBinary(bi)

val newImg = createTIFFImage(newBi, newBi.getWidth, newBi.getHeight)

val newF = new File("C:/Users/dprice/Desktop/ME Test/2020002863.014a.tif")

TIFFImage.toFile(newF, List(newImg))
```
