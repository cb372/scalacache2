
val commonSettings = Seq(
  organization := "org.scalacache",
  scalaVersion := "2.12.2"
)

val moduleSettings = commonSettings

lazy val root = Project("root", file("."))
  .settings(commonSettings)
  .aggregate(core, caffeine)

def module(name: String) = Project(s"scalacache2-$name", file(s"modules/$name"))
  .settings(moduleSettings)

lazy val core = module("core")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "0.9.0"
    )
  )

lazy val caffeine = module("caffeine")
  .settings(
    libraryDependencies ++= Seq(
      "com.github.ben-manes.caffeine" % "caffeine" % "2.5.2"
    )
  ).dependsOn(core)
