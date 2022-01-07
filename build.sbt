scalaVersion := "3.1.0"

lazy val wb = (project in file("."))
 .settings(
  name := "wb",
  libraryDependencies ++= Seq(
   "com.monovore"     %% "decline-effect"      % "2.2.0",
   "es.weso"          %% "shex"                % "0.1.108",
   "es.weso"          %% "srdfjena"            % "0.1.106",
   "org.apache.jena"  %  "jena-shex"           % "4.3.2", 
   "org.http4s"       %% "http4s-ember-client" % "1.0.0-M30",
   "org.slf4j"        % "slf4j-api"            % "1.7.3",
   "org.slf4j"        % "slf4j-simple"         % "1.7.3",
   "org.typelevel"    %% "cats-effect"         % "3.3.3",
   "org.typelevel"    %% "cats-effect-std"     % "3.3.3"
  ),
  run / fork := true
 )


/* lazy val docs = project 
  .in(file("wb-docs")) // important: it must not be docs/
  .enablePlugins(MdocPlugin, DocusaurusPlugin) 

*/  

