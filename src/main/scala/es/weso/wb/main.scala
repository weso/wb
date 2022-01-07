package es.weso.wb
import com.monovore.decline._
import com.monovore.decline.effect._
import cats.effect._
import cats.implicits._
import Info._
import org.http4s.ember.client._
import org.http4s.client._
import org.http4s.client.middleware._
import scala.concurrent.ExecutionContext.global


object Main extends CommandIOApp(
  name    = "wb",
  version = "0.0.1",
  header  =
     s"""|Information about Wikidata or Wikibase instances
         |
         |Example: wb info --schema E42 
         |  Prints information about entity schema E42 from Wikidata
         |""".stripMargin.trim,
) {

 def runWithContext(wbc: WBCommand): IO[ExitCode] = 
   EmberClientBuilder
   .default[IO]
   .build
   .use { 
     client => wbc.run(Context(FollowRedirect(5)(client))) 
   }

 val wbCommand: Opts[WBCommand] = 
   Info.infoCommand orElse Validate.validateCommand orElse Sparql.sparqlCommand

 def main: Opts[IO[ExitCode]] =
   wbCommand.map{ case wbc => runWithContext(wbc) }

}