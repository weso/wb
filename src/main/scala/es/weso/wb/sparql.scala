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

case class Sparql(
 query: Query, 
 wikibase: Wikibase, 
 queryResultFormat: QueryResultFormat,
 verbose:  VerboseLevel) {

  def run(client: Client[IO]): IO[ExitCode] = for {
    queryStr <- query match {
      case QueryPath(path) => getContents(path)
    }
    request = Request[IO](
      uri = wikibase
            .sparqlEndpoint
            .withQueryParam("query",queryStr))
            .addHeader(Accept(MediaType.application.`sparql-results+xml`))
    str <- client.expect[String](request)
    _ <- IO.println(s"Result\n$str")
  } yield ExitCode.Success

}

object Sparql {

 val sparqlCommand: Opts[Sparql] =
    Opts.subcommand("sparql", "Run SPARQL query") {
      (Query.query, 
       Wikibase.wikibase, 
       QueryResultFormat.queryResultFormat, 
       Verbose.verbose
      ).mapN(Sparql.apply)
 }

 
}
