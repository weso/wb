package es.weso.wb

import java.nio.file.Path
import io.circe.syntax._

/**
 * wb Errors
 **/
case class NoSchemaWikibase(wb: Wikibase) 
 extends RuntimeException(s"No entity schema template for wikibase: ${wb}")
case class ErrorNoTemplate(wb: Wikibase) 
 extends RuntimeException(s"No entityTemplate specified for wikibase: $wb")
case class NoSparqlEndpoint(wb: Wikibase) 
 extends RuntimeException(s"No SPARQL endpoint specified for wikibase: $wb")
case class ErrorUri(str: String, exc: Throwable) 
 extends RuntimeException(s"Error building uri from ${str}: ${exc.getMessage()}")
case class ParsingJsonError(path: Path, str: String, exc: Throwable) 
 extends RuntimeException(s"Error parsing json content ${str}: ${exc.getMessage()}")
case class NotFoundWikibase(name: String, wbs: List[Wikibase], path: Path) 
 extends RuntimeException(s"Not found wikibase with name: $name\nAvailable wikiabses: ${wbs.map(_.asJson.spaces2)}\nPath: $path")