name         := "suite-scala-sdk"
organization := "com.emarsys"
version      := "0.0.4"
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
  val akkaV       = "2.4.10"
  val scalaTestV  = "3.0.0"
  Seq(
    "com.typesafe.akka"     %% "akka-stream"                       % akkaV,
    "com.typesafe.akka"     %% "akka-http-core"                    % akkaV,
    "com.typesafe.akka"     %% "akka-http-experimental"            % akkaV,
    "com.typesafe.akka"     %% "akka-http-spray-json-experimental" % akkaV,
    "com.github.fommil"     %% "spray-json-shapeless"              % "1.2.0",
    "org.slf4j"             %  "slf4j-nop"                         % "1.6.4",
    "org.scalatest"         %% "scalatest"                         % scalaTestV % "test",
    "com.emarsys"           %% "escher-akka-http"                  % "0.0.7"
  )
}

publishTo := Some(Resolver.file("releases", new File("releases")))
