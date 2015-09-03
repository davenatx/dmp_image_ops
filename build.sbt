import Dependencies._

name := "dmp_image_ops"

organization := "com.dmp"

version := "0.1"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-optimize", "-deprecation", "-feature")

resolvers ++= Seq("Github Repo" at "http://davenatx.github.io/maven")

libraryDependencies ++= Dependencies.imageOps

git.baseVersion := "0.1"

//versionWithGit

showCurrentGitBranch

scalariformSettings

org.scalastyle.sbt.ScalastylePlugin.Settings