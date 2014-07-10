name := "gamecenter"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.3.0",
  jdbc,
  anorm,
  cache,
  "se.radley" %% "play-plugins-salat" % "1.4.0" withSources(),
  "com.typesafe.akka" %% "akka-testkit" % "2.2.4" % "test" withSources(),
  "com.ning" % "async-http-client" % "1.8.3" % "test" withSources(),
  "org.apache.httpcomponents" % "httpclient" % "4.3.3" % "test" withSources(),
  "com.belerweb" % "weibo4j-oauth2" % "2.1.1-beta2-3" withSources()
)     

unmanagedSourceDirectories in Compile += new File(baseDirectory.value+"/src/main/scala")

unmanagedSourceDirectories in Compile += new File(baseDirectory.value+"/src/main/resources")

play.Project.playScalaSettings
