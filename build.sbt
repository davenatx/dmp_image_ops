import Dependencies._

name := "dmp_image_ops"

organization := "com.dmp"

version := "0.3"

scalaVersion := "2.11.12"

scalacOptions ++= Seq("-optimize", "-deprecation", "-feature")

resolvers ++= Seq("Github Repo" at "http://davenatx.github.io/maven")

libraryDependencies ++= Dependencies.imageOps

git.baseVersion := "1.0"

//versionWithGit

showCurrentGitBranch