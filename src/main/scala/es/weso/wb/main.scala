package es.weso.wb
import com.monovore.decline._
import com.monovore.decline.effect._
import cats.effect._
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

 def runWithClient(f: Client[IO] => IO[ExitCode]): IO[ExitCode] = 
   EmberClientBuilder
   .default[IO]
   .build
   .use { client => f(FollowRedirect(5)(client)) } 

 def main: Opts[IO[ExitCode]] =
   (Info.infoCommand orElse Validate.validateCommand).map {
        case ic: Info => runWithClient(ic.run)
        case vc: Validate => runWithClient(vc.run)
   }

}