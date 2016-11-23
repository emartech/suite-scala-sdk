name         := "suite-scala-sdk"
organization := "com.emarsys"
version      := "0.0.6"
scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-feature",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Ywarn-dead-code",
  "-Xlint",
  "-Xfatal-warnings"
)

resolvers += "escher-akka-http on GitHub" at "https://raw.github.com/emartech/escher-akka-http/master/releases"

libraryDependencies ++= {
  val akkaHttpV   = "10.0.0"
  val scalaTestV  = "3.0.1"
  Seq(
    "com.typesafe.akka"     %% "akka-http-core"       % akkaHttpV,
    "com.typesafe.akka"     %% "akka-http"            % akkaHttpV,
    "com.typesafe.akka"     %% "akka-http-spray-json" % akkaHttpV,
    "com.github.fommil"     %% "spray-json-shapeless" % "1.3.0",
    "org.slf4j"             %  "slf4j-nop"            % "1.6.4",
    "org.scalatest"         %% "scalatest"            % scalaTestV % "test",
    "com.emarsys"           %% "escher-akka-http"     % "0.0.9"
  )
}

publishTo := Some(Resolver.file("releases", new File("releases")))
