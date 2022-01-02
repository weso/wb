package es.weso.wb

import cats._
import cats.implicits._
import cats.effect._
import com.monovore.decline._
import scala.jdk.CollectionConverters._
import org.http4s.{Charset => _, _}
import java.nio.charset.Charset
import org.http4s.client._
import es.weso.rdf.jena._
import es.weso.shex._
import es.weso.shapemaps.ShapeMap
import es.weso.shex.validator.Validator
import es.weso.utils.IOUtils.fromES
import ShExEngine.{ ShExS => ShExSEngine, _}
import es.weso.shexjena._
import org.apache.jena.shex.sys.ShexLib
import EntitySchema._
import java.nio.file.Paths
import java.nio.file.Files
import es.weso.utils.FileUtils._

case class Validate(
  schemaRef: EntitySchema, 
  entityId: EntityId, 
  wikibase: Wikibase, 
  shape: Option[String],
  engine: ShExEngine,
  resultFormat: ResultFormat,
  verbose: VerboseLevel) {

 def resolveSchema(client: Client[IO], verbose: VerboseLevel): IO[Schema] = schemaRef match {
   case EntitySchemaRef(sid) => wikibase.findSchema(sid.toString, client, verbose)
   case EntitySchemaPath(path) => for {
     schemaStr <- getContents(path) // IO.blocking { Files.readAllLines(path, Charset.forName("UTF-8")).asScala.mkString("\n") } 
     schema <- Schema.fromString(schemaStr, "SHEXC", None, None)
   } yield schema  
 }

 def resolveSchemaStr(client: Client[IO], verbose: VerboseLevel): IO[String] = schemaRef match {
   case EntitySchemaRef(sid) => wikibase.findSchemaStr(sid.toString, client, verbose)
   case EntitySchemaPath(path) => getContents(path)
 }

 def run(client: Client[IO]): IO[ExitCode] = engine match {
    case ShExSEngine => runShExS(client)
    case Jena => runShExJena(client)
 }

 def runShExS(client: Client[IO]): IO[ExitCode] = { 
  
  val shapeMapStr = "<" + wikibase.entityTemplate + entityId + ">@" + (shape match {
     case None => "start"
     case Some(s) => s
  })
 
  for {
    schema <- resolveSchema(client, verbose)
    rdfStr <- wikibase.findEntity(entityId, client, InfoMode.Raw, verbose)
    res1 <- RDFAsJenaModel.fromString(rdfStr, "Turtle", None)
    res2 <- RDFAsJenaModel.empty
    result <- (res1,res2).tupled.use {
        case (rdf,builder) => for {
             rdfPrefixMap <- rdf.getPrefixMap
             resolvedSchema <- ResolvedSchema.resolve(schema,None)
             shapeMap <- fromES(ShapeMap.fromCompact(shapeMapStr, schema.base, schema.prefixMap).leftMap(_.toList.mkString("\n")))
             fixedShapeMap <- ShapeMap.fixShapeMap(shapeMap, rdf, rdfPrefixMap, schema.prefixMap)
             r <- Validator.validate(resolvedSchema, fixedShapeMap, rdf, builder, verbose.toBoolean )
             resultShapeMap <- r.toResultShapeMap
            } yield resultShapeMap
    } 
     _ <- IO.println(s"Result ShEx-S validation:")
     _ <- IO.println(result.serialize(resultFormat.name).fold(
      err => s"Error serializing ${result} with format ${resultFormat.name}: $err", 
      identity)
    )
  } yield ExitCode.Success
 }

 def runShExJena(client: Client[IO]): IO[ExitCode] = for {
   rdfStr <- wikibase.findEntity(entityId, client, InfoMode.Raw, verbose)
   res1 <- RDFAsJenaModel.fromString(rdfStr, "Turtle", None)
   res2 <- RDFAsJenaModel.empty
   report <- (res1,res2).tupled.use {
    case (rdf,builder) => for {
      schema <- resolveSchemaStr(client, verbose)
      node = wikibase.entityTemplate + entityId
      r <- ShExJena.validate(rdf, schema, node, shape)
     } yield r 
   }
   _ <- IO.println(s"Result ShEx-Jena validation") 
   str <- ShExJena.report2Str(report)
   _ <- IO.println(str)
 } yield ExitCode.Success  

}

object Validate {

 val validateCommand: Opts[Validate] =
    Opts.subcommand("validate", "Validate an entity with an entity schema") {
      (EntitySchema.entitySchema, 
       EntityId.entityId, 
       Wikibase.wikibase, 
       ShapeOpt.shape, 
       ShExEngine.shexEngine, 
       ResultFormat.resultFormat,       
       Verbose.verbose).mapN(Validate.apply)
 }
 
}
