import sbt._
import sbt.Keys._

object Version {
  val scalaLogging = "3.8.0"
  val logback      = "1.2.3"
  val config       = "1.3.3"
  val jaiImageio   = "1.2-pre-dr-b04-2012-05-17"
  val specs2       = "4.0.3"
}

object Library {
  val scalaLogging =  "com.typesafe.scala-logging" 	%% "scala-logging" 	  % Version.scalaLogging
  val logback      = 	"ch.qos.logback" 				      %  "logback-classic"  % Version.logback
  val config       = 	"com.typesafe" 				        %  "config" 			    % Version.config
  val jaiImageio   =  "com.github.davenatx" 			  %  "jai-imageio-core" % Version.jaiImageio
  val specs2       =  "org.specs2"                 %%  "specs2-core"      % Version.specs2
}

object Dependencies {
  import Library._
  
  val imageOps = List(
    scalaLogging,
	 logback,
	 config,
	 jaiImageio,
   specs2
  )
}