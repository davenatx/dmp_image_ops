import Dependencies._

name := "dmp_image_ops"

organization := "com.dmp"

version := "0.6"

scalaVersion := "2.12.11"

scalacOptions ++= Seq("-deprecation", "-feature")

resolvers += "DMP's Gitub Repo" at "http://davenatx.github.io/maven"

libraryDependencies ++= Dependencies.imageOps

git.baseVersion := "1.0"

//versionWithGit

showCurrentGitBranch