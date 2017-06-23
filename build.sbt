
val commonSettings = Seq(
  organization := "org.scalacache",
  scalaVersion := "2.12.2"
)

val moduleSettings = commonSettings ++ Seq(
  artifactName := {
    val previous = artifactName.value
    (a, b, c) => s"scalacache2-${previous(a, b, c)}"
  },
  libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.7.25"
  )
)

lazy val root = project.in(file("."))
  .settings(commonSettings)
  .aggregate(core, caffeine, memcached)

def module(name: String) = Project(s"$name", file(s"modules/$name"))
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

lazy val memcached = module("memcached")
  .settings(
    libraryDependencies ++= Seq(
      "net.spy" % "spymemcached" % "2.12.1"
    )
  ).dependsOn(core)
