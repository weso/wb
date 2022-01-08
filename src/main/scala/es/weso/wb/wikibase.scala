package es.weso.wb
import com.monovore.decline._
import java.net.URI
import scala.util.Try
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.implicits._
import org.http4s.client._
import org.http4s.implicits.uri
import es.weso.shex.{Path => _, _}
import es.weso.rdf._
import es.weso.rdf.jena._
import es.weso.rdf.nodes._
import org.http4s.Method._
import org.http4s.headers._
import org.http4s.client.dsl.io._
import Verbose._
import io.circe._
import io.circe.syntax._
import es.weso.utils.json.DecoderUtils._
import es.weso.utils._
import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.charset.Charset
import scala.jdk.CollectionConverters._
import java.nio.file.NoSuchFileException

sealed abstract class WikibaseRef

case class Wikibase(
  name: Option[String],
  sparqlEndpoint: Option[Uri], 
  root: Option[Uri],
  schemaTemplate: Option[String]
  ) extends WikibaseRef {

  val entityTemplate = root.map(_.toString + "/entity/")

  override def toString = 
    s"Wikibase(name = ${name.getOrElse("")}, endpoint=${sparqlEndpoint.getOrElse("")}, entityTeplate: ${entityTemplate.getOrElse("")}, entitySchemaTemplate=${schemaTemplate.getOrElse("")})"  

  def toJson = this.asJson


  def findSchemaStr(id: String, client: Client[IO], verbose: VerboseLevel): IO[String] = 
   schemaTemplate match {
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

  def findEntity(id: EntityId, client: Client[IO], mode: InfoMode, verbose: VerboseLevel): IO[String] = 
   entityTemplate match {
     case None => IO.raiseError(ErrorNoTemplate(this))
     case Some(et) => {
      val entityUri = et + id.toString
      Uri.fromString(entityUri).fold(
        exc => IO.raiseError(ErrorUri(entityUri,exc)),
        uri => for { 
          _ <- info(s"Request: $uri", verbose)
          str <- client.expect[String](uri)
          n = 10
          _ <- infoVerbose(s"Raw contents ($n lines): ${str.split('\n').take(n).mkString("\n")}\n...", verbose)
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

case class WikibaseByName(name: String) extends WikibaseRef

object Wikibase {

  lazy val wikidata: Wikibase = 
    Wikibase(
     name = Some("wikidata"),
     sparqlEndpoint = Some(uri"https://query.wikidata.org/sparql"), 
     root = Some(uri"http://www.wikidata.org"),
     schemaTemplate = Some("https://www.wikidata.org/wiki/Special:EntitySchemaText/")
    )  

  /* lazy val rhizome: Wikibase = 
    Wikibase(
     name = Some("rhizome"), 
     entityTemplate = Some("http://artbase.rhizome.org/entity/"), 
     sparqlEndpoint = Some(uri"https://query.artbase.rhizome.org/proxy/wdqs/bigdata/namespace/wdq/sparql"), 
     schemaTemplate = None
  ) */

/*  lazy val gndtest: Wikibase = 
    Wikibase(
     name = Some("gndtest"), 
     entityTemplate = Some("http://gndtest.wiki.opencura.com/entity/"),
     sparqlEndpoint = Some(uri"https://gndtest.wiki.opencura.com/query/sparql"), 
     schemaTemplate = Some("https://gndtest.wiki.opencura.com/wiki/Special:EntitySchemaText/")
  ) */

  lazy val availableWikibases = List(wikidata)
  lazy val defaultWikibase = availableWikibases.head
  lazy val availableWikibaseStrs = availableWikibases.map(_.name)
  lazy val availableWikibaseStr = availableWikibases.map(_.name).mkString("|")

  def wbname: Opts[Option[String]] = 
    Opts.option[String](
     "wbname", 
     metavar="name", 
     help="wikibase name").orNone

  def wbendpoint: Opts[Option[Uri]] = 
    Opts.option[String]("wbendpoint", metavar="uri", help = "wikibase endpoint. Example: https://query.wikidata.org/sparql")
    .mapValidated(s => Uri.fromString(s).fold(
      failure => Validated.invalidNel(s"Error parsing URI $s: $failure"), 
      uri => Validated.valid(uri)
    )).orNone

  def wbRoot: Opts[Option[Uri]] = 
    Opts.option[String](
    "wbroot", 
    metavar="uri", 
    help = "wikibase root. Example: http://www.wikidata.org")
    .mapValidated(s => Uri.fromString(s).fold(
      failure => Validated.invalidNel(s"Error parsing URI $s: $failure"), 
      uri => Validated.valid(uri)
    )).orNone

  def wbentitySchemaTemplate: Opts[Option[String]] = 
    Opts.option[String](
    "wbschema", 
    metavar="template", 
    help = "wikibase entity schema template. Example: https://www.wikidata.org/wiki/Special:EntitySchemaText/"
   ).orNone

  def wikibaseParts: Opts[Wikibase] =
    (wbname,wbendpoint,wbRoot,wbentitySchemaTemplate)
    .tupled
    .mapValidated(s => s match {
      case (None, None, None, None) => "None of the wb parameters has been set".invalidNel
      case (n,e,et,st) => Wikibase(n,e,et,st).validNel
  })

  def wikibaseByName: Opts[WikibaseRef] = 
    Opts.option[String](
        "wikibase", 
        short = "w",
        metavar = "name",
        help =s"Wikibase, default: ${defaultWikibase.name}")
    .map(WikibaseByName.apply)
    .withDefault(defaultWikibase)

  def wikibase: Opts[WikibaseRef] = wikibaseByName orElse wikibaseParts

  // TODO: Replace this by automatically generated encoders/decoders
  // At this moment it seems to fail in Scala 3
  implicit val wikibaseEncoder: Encoder[Wikibase] = new Encoder[Wikibase] {
    final def apply(wb: Wikibase): Json = 
      mkObject(List(
        optField("name", wb.name.map(_.asJson)),
        optField("endpoint", wb.sparqlEndpoint.map(_.toString.asJson)),
        optField("entity-template", wb.entityTemplate.map(_.asJson)),
        optField("schema-template", wb.schemaTemplate.map(_.asJson))
      ))
  }

  implicit val wikibaseDecoder: Decoder[Wikibase] = Decoder.instance { c =>
    for {
      name <- optFieldDecode[String](c, "name")
      optSparqlEndpointStr <- optFieldDecode[String](c, "endpoint")
      sparqlEndpoint <- optSparqlEndpointStr match { 
        case None => Right(None)
        case Some(str) => Uri.fromString(str).fold(
        fa => Left(DecodingFailure(s"Error decoding sparql endpoint. Cannot build URI from string: $str", Nil)), 
        uri => Right(Some(uri)))
      }
      rootStr <- optFieldDecode[String](c,"root")
      root <- rootStr match { 
        case None => Right(None)
        case Some(str) => Uri.fromString(str).fold(
        fa => Left(DecodingFailure(s"Error decoding root. Cannot build URI from string: $str", Nil)), 
        uri => Right(Some(uri)))
      }
      schemaTemplate <- optFieldDecode[String](c,"schema-template")
    } yield Wikibase(name, sparqlEndpoint, root, schemaTemplate)
  }
 
  def optField[A: Encoder](name: String, m: Option[A]): Option[(String, Json)] = {
    m match {
      case None => None
      case Some(v) => field(name, v)
    }
  }

  def field[A: Encoder](name: String, v: A): Option[(String, Json)] = {
    val encoder = implicitly[Encoder[A]]
    Some((name, encoder(v)))
  }

  def mkObject(fields: List[Option[(String, Json)]]): Json = {
    val fs = fields.filter(_.isDefined).sequence
    val map = fs.getOrElse(List()).toMap
    Json.fromJsonObject(JsonObject.fromMap(map))
  }

  def loadWikibases(path: Path): IO[List[Wikibase]] = 
    for {
      contents <- getContents(path)
      wikibases <- io.circe.parser.decode[List[Wikibase]](contents).fold(
        err => IO.raiseError(ParsingJsonError(path,err.getMessage, err.getCause)),
        IO.pure(_)
      )
    } yield List(wikidata) ++ wikibases

  def getContents(path:Path): IO[String] = if (path == null) {
   IO.pure("[]") 
  } else IO.blocking {
   Files.readAllLines(path, Charset.forName("UTF-8")).asScala.toList.mkString("\n")
  }.recover {
    case _: NoSuchFileException => "[]"
  }

  def getWikibase(wbr: WikibaseRef, path: Path, verbose: VerboseLevel): IO[Wikibase] = wbr match {
    case wb: Wikibase => IO.pure(wb)
    case wbn: WikibaseByName => for {
      _ <- info(s"getWikibase by name: $wbn, path: $path", verbose)
      wbs <- loadWikibases(path)
      name = wbn.name
      wb <- findByName(name, wbs) match {
        case None => IO.raiseError(NotFoundWikibase(name,wbs,path))
        case Some(wb) => IO.pure(wb)
      }
    } yield wb
  }

  def findByName(name: String, wbs: List[Wikibase]): Option[Wikibase] = 
    wbs.collect { wb => wb.name match {
      case Some(n) if (n.toLowerCase == name.toLowerCase) => wb
     }}.headOption

}