ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.12.17"
//ThisBuild / scalaVersion := "2.11.8"
//ThisBuild / scalaVersion := "2.12.16"
//ThisBuild / scalaVersion := "2.12.15"
//ThisBuild / scalaVersion := "2.13.12"
//ThisBuild / scalaVersion := "2.13.6"
//ThisBuild / scalaVersion := "2.13.4"
ThisBuild / organization := "org.example"

val spinalVersion = (
  //"1.9.4"
  //"dev"
  //"1.10.0"
  //"1.10.2a"
  "1.12.2"
)
val spinalCore = "com.github.spinalhdl" %% "spinalhdl-core" % spinalVersion
val spinalLib = "com.github.spinalhdl" %% "spinalhdl-lib" % spinalVersion
val spinalIdslPlugin = compilerPlugin("com.github.spinalhdl" %% "spinalhdl-idsl-plugin" % spinalVersion)

//scalacOptions += "-Ybackend-parallelism 4"
//scalacOptions += "-Ybackend-parallelism 4"

lazy val libcheesevoyage = (project in file("."))
  .settings(
    Compile / scalaSource := baseDirectory.value / "hw" / "spinal",
    libraryDependencies ++= Seq(spinalCore, spinalLib, spinalIdslPlugin),
    //scalacOptions += "-Ybackend-parallelism 4"
    scalacOptions ++= Seq(
      "-Ybackend-parallelism", "4",
      "-Ybackend-worker-queue", "4",
      "-P:semanticdb:sourceroot:."
    )
  )

fork := true
