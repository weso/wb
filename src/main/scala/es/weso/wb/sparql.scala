package es.weso.wb
import cats._
import cats.implicits._
import cats.effect._
import com.monovore.decline._
import org.http4s.{Query => _, _}
import org.http4s.client._
import es.weso.rdf.jena._
import es.weso.shex._
import es.weso.wb.Verbose._
import es.weso.utils.FileUtils._
import Query._
import org.apache.jena.query.QueryExecution
import org.http4s.headers._
import org.http4s.MediaType
import es.weso.utils.VerboseLevel

case class Sparql(
 query: Query, 
 queryResultFormat: QueryResultFormat,
 wikibaseRef: WikibaseRef,
 wikibasesPath: WikibasesPath,
 verbose:  VerboseLevel) extends WBCommand {

def run(ctx: Context): IO[ExitCode] = for {
  wikibase <- Wikibase.getWikibase(wikibaseRef, wikibasesPath.path, verbose)
  r <- wikibase.sparqlEndpoint match {
    case None => IO.raiseError(NoSparqlEndpoint(wikibase))
    case Some(endpointUri) => for {
    queryStr <- query match {
      case QueryPath(path) => getContents(path)
    }
    request = Request[IO](
      uri = endpointUri
            .withQueryParam("query",queryStr))
            .addHeader(Accept(MediaType.application.`sparql-results+xml`))
    str <- ctx.client.expect[String](request)
    _ <- IO.println(s"Result\n$str")
   } yield ExitCode.Success
  }
 } yield r
}

object Sparql {

 val sparqlCommand: Opts[Sparql] =
    Opts.subcommand("sparql", "Run SPARQL query") {
      (Query.query, 
       QueryResultFormat.queryResultFormat, 
       Wikibase.wikibase, 
       WikibasesPath.path,
       Verbose.verbose
      ).mapN(Sparql.apply)
 }

 
}
