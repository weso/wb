package es.weso.wb
import com.monovore.decline._
import java.net.URI
import scala.util.Try
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.client._
// import org.http4s.Uri
import org.http4s.implicits.uri
import es.weso.shex._
import es.weso.rdf._
import es.weso.rdf.jena._
import es.weso.rdf.nodes._
import org.http4s.Method._
import org.http4s.headers._
import org.http4s.client.dsl.io._

case class NoSchemaWikibase(wb: Wikibase) extends RuntimeException(s"No entity schema template for wikibase: ${wb.name}")
case class ErrorUri(str: String, exc: Throwable) extends RuntimeException(s"Error building uri from ${str}: ${exc.getMessage()}")

case class Wikibase(
  name: String,
  sparqlEndpoint: Uri, 
  entityTemplate: String,
  schemaTemplate: Option[String]
  ) {

  def findSchemaStr(id: String, client: Client[IO]): IO[String] = schemaTemplate match {
   case None => IO.raiseError(NoSchemaWikibase(this))
   case Some(template) => {
    val requestStr = template + id
    Uri.fromString(requestStr).fold(
      exc => IO.raiseError(ErrorUri(requestStr,exc)),
      uri => client.expect[String](uri)
   )
   }
  }

  def findSchema(id: String, client: Client[IO]): IO[Schema] = for {
    schemaStr <- findSchemaStr(id, client)
    schema <- Schema.fromString(schemaStr, "SHEXC", None, None)
  } yield schema

  def findEntity(id: EntityId, client: Client[IO]): IO[String] = {
    val requestStr = entityTemplate + id.toString
    Uri.fromString(requestStr).fold(
      exc => IO.raiseError(ErrorUri(requestStr,exc)),
      uri => {
        val request = Request(Method.GET, uri).putHeaders(Accept(MediaType.application.json))
        for { 
          str <- client.expect[String](uri) 
          rdf <- RDFAsJenaModel.fromString(str, "TURTLE", None)
                 .flatMap(_.use(rdf => 
                   rdf.serialize("TURTLE")))
        } yield rdf
      }           
    )
  } 
    
}


object Wikibase {

  lazy val wikidata: Wikibase = 
    Wikibase("wikidata",
      uri"https://query.wikidata.org/sparql", 
      entityTemplate = "https://www.wikidata.org/entity/",
      schemaTemplate = Some("https://www.wikidata.org/wiki/Special:EntitySchemaText/")
    )  

  lazy val rhizome: Wikibase = 
    Wikibase(
     name = "rhizome", 
     entityTemplate = "https://artbase.rhizome.org/entity/", 
     sparqlEndpoint = uri"https://query.artbase.rhizome.org/proxy/wdqs/bigdata/namespace/wdq/sparql", 
     schemaTemplate = None
  )

  lazy val gndtest: Wikibase = 
    Wikibase(
     name = "gndtest", 
     entityTemplate = "http://gndtest.wiki.opencura.com/entity/",
     sparqlEndpoint = uri"https://gndtest.wiki.opencura.com/query/sparql", 
     schemaTemplate = Some("https://gndtest.wiki.opencura.com/wiki/Special:EntitySchemaText/")
  )

  lazy val availableWikibases = List(wikidata,rhizome,gndtest)
  lazy val defaultWikibase = availableWikibases.head

  def wikibase: Opts[Wikibase] = 
    Opts.option[String](
        "wikibase", 
        help =s"Wikibase Instance, default = $defaultWikibase")
    .mapValidated(s => 
      availableWikibases.find(_.name == s) match {
        case None => Validated.invalidNel(s"Error obtaining wikibase. Available values = ${availableWikibases.map(_.name).mkString(",")}") 
        case Some(wb) => Validated.valid(wb)
      })
    .withDefault(defaultWikibase)

}