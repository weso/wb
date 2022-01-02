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
import org.http4s.implicits.uri
import es.weso.shex._
import es.weso.rdf._
import es.weso.rdf.jena._
import es.weso.rdf.nodes._
import org.http4s.Method._
import org.http4s.headers._
import org.http4s.client.dsl.io._
import Verbose._

case class NoSchemaWikibase(wb: Wikibase) extends RuntimeException(s"No entity schema template for wikibase: ${wb.name}")
case class ErrorUri(str: String, exc: Throwable) extends RuntimeException(s"Error building uri from ${str}: ${exc.getMessage()}")

case class Wikibase(
  name: String,
  sparqlEndpoint: Uri, 
  entityTemplate: String,
  schemaTemplate: Option[String]
  ) {

  def findSchemaStr(id: String, client: Client[IO], verbose: VerboseLevel): IO[String] = schemaTemplate match {
   case None => IO.raiseError(NoSchemaWikibase(this))
   case Some(template) => {
    val requestStr = template + id
    for {
      _ <- info(s"request uri: $requestStr", verbose)
      str <- Uri.fromString(requestStr).fold(
       exc => IO.raiseError(ErrorUri(requestStr,exc)),
       uri => client.expect[String](uri)
     )
    } yield str
   }
  }

  def findSchema(id: String, client: Client[IO], verbose: VerboseLevel): IO[Schema] = for {
    schemaStr <- findSchemaStr(id, client, verbose)
    schema <- Schema.fromString(schemaStr, "SHEXC", None, None)
  } yield schema

  def findEntity(id: EntityId, client: Client[IO], mode: InfoMode, verbose: VerboseLevel): IO[String] = {
    val entityUri = entityTemplate + id.toString
    Uri.fromString(entityUri).fold(
      exc => IO.raiseError(ErrorUri(entityUri,exc)),
      uri => for { 
          _ <- info(s"Request: $uri", verbose)
          str <- client.expect[String](uri)
          n = 10
          _ <- info(s"Raw contents ($n lines): ${str.split('\n').take(n).mkString("\n")}\n...", verbose)
          rdfResource <- RDFAsJenaModel.fromString(str, "TURTLE", None)
          builder <- RDFAsJenaModel.empty
          rdfStr <- (rdfResource, builder).tupled.use{ 
           case (rdf, builder) => for {
            _ <- info(s"RDF parsed ok!", verbose)
            cleaned <- if (mode == InfoMode.Out) {
              cleanRdf(rdf, builder, entityUri, verbose)
            } else IO.pure(rdf)
            rdfStr <- cleaned.serialize("TURTLE")
            _ <- info(s"RDF serialized ok!", verbose)
          } yield rdfStr
         }
        } yield rdfStr
    )
  } 

  /**
   * Creates a copy of an RDF graph which contains only the outgoing triples from a focus node
   **/
  def cleanRdf(
   rdf: RDFAsJenaModel, 
   builder: RDFAsJenaModel, 
   focus: String,
   verbose: VerboseLevel
   ): IO[RDFAsJenaModel] = {
    val focusNode = IRI(focus)
    for {
      ts <- rdf.triplesWithSubject(focusNode).compile.toList
      pm <- rdf.getPrefixMap
      _ <- infoVerbose(s"Triples added: ${ts.size}. FocusNode: ${focusNode}", verbose)
      _ <- builder.addPrefixMap(pm)
      _ <- builder.addTriples(ts.toSet)
    } yield builder
  }
}


object Wikibase {

  lazy val wikidata: Wikibase = 
    Wikibase("wikidata",
      uri"https://query.wikidata.org/sparql", 
      entityTemplate = "http://www.wikidata.org/entity/",
      schemaTemplate = Some("https://www.wikidata.org/wiki/Special:EntitySchemaText/")
    )  

  lazy val rhizome: Wikibase = 
    Wikibase(
     name = "rhizome", 
     entityTemplate = "http://artbase.rhizome.org/entity/", 
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
  lazy val availableWikibaseStrs = availableWikibases.map(_.name)
  lazy val availableWikibaseStr = availableWikibases.map(_.name).mkString("|")

  def wikibase: Opts[Wikibase] = 
    Opts.option[String](
        "wikibase", 
        help =s"Wikibase, default: ${defaultWikibase.name}, values: ${availableWikibaseStr}")
    .mapValidated(s => 
      availableWikibases.find(_.name.toLowerCase == s.toLowerCase) match {
        case None => Validated.invalidNel(s"Error obtaining wikibase. Available values = ${availableWikibaseStr}") 
        case Some(wb) => Validated.valid(wb)
      })
    .withDefault(defaultWikibase)

}