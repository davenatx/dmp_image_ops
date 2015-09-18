package com.dmp

import com.typesafe.config.ConfigFactory

package object image {
  private val config = ConfigFactory.load("settings.properties")

  lazy val defaultXResolution = config.getInt("defaultXResolution")
  lazy val defaultYResolution = config.getInt("defaultYResolution")
  lazy val overlayImageType = config.getInt("overlayImageType")
  lazy val compressionType = config.getString("compressionType")
  lazy val resolutionUnit = config.getInt("resolutionUnit")
  lazy val softwareTag = config.getString("softwareTag")
}