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
import es.weso.wikibaserdf.WikibaseRDF
import Verbose._
import es.weso.rdf.nodes.IRI

case class Validate(
  schemaRef: EntitySchema, 
  entityId: EntityId, 
  shape: Option[String],
  engine: ShExEngine,
  mode: ValidateMode,
  resultFormat: ResultFormat,
  wikibaseRef: WikibaseRef,
  wikibasesPath: WikibasesPath,
  verbose: VerboseLevel
  ) extends WBCommand {

 def resolveSchema(client: Client[IO], wikibase: Wikibase, verbose: VerboseLevel): IO[Schema] = schemaRef match {
   case EntitySchemaRef(sid) => wikibase.findSchema(sid.toString, client, verbose)
   case EntitySchemaPath(path) => for {
     schemaStr <- getContents(path) // IO.blocking { Files.readAllLines(path, Charset.forName("UTF-8")).asScala.mkString("\n") } 
     schema <- Schema.fromString(schemaStr, "SHEXC", None, None)
   } yield schema  
 }

 def resolveSchemaStr(client: Client[IO], wikibase: Wikibase, verbose: VerboseLevel): IO[String] = schemaRef match {
   case EntitySchemaRef(sid) => wikibase.findSchemaStr(sid.toString, client, verbose)
   case EntitySchemaPath(path) => getContents(path)
 }

 def run(ctx: Context): IO[ExitCode] = for {
   wikibase <- Wikibase.getWikibase(wikibaseRef, wikibasesPath.path, verbose)
   r <- engine match {
    case ShExSEngine => runShExS(ctx.client, wikibase)
    case Jena => runShExJena(ctx.client, wikibase)
   }
  } yield r
   

 def runShExS(client: Client[IO], wikibase: Wikibase): IO[ExitCode] = { 
  
  val shapeMapStr = "<" + wikibase.entityTemplate.getOrElse("") + entityId + ">@" + (shape match {
     case None => "start"
     case Some(s) => s
  })
 
  for {
    schema <- resolveSchema(client, wikibase, verbose)
    resolvedSchema <- ResolvedSchema.resolve(schema,None)
    shapeMap <- fromES(ShapeMap.fromCompact(shapeMapStr, schema.base, schema.prefixMap).leftMap(_.toList.mkString("\n")))
    r <- mode match {
      case ValidateMode.Simple => for {
       rdfStr <- wikibase.findEntity(entityId, client, InfoMode.Raw, verbose)
       res1 <- RDFAsJenaModel.fromString(rdfStr, "Turtle", None)
       res2 <- RDFAsJenaModel.empty
       result <- (res1,res2).tupled.use {
        case (rdf,builder) => for {
          rdfPrefixMap <- rdf.getPrefixMap
          fixedShapeMap <- ShapeMap.fixShapeMap(shapeMap, rdf, rdfPrefixMap, schema.prefixMap)
          r <- Validator.validate(resolvedSchema, fixedShapeMap, rdf, builder, verbose.toBoolean )
        } yield r
       }
      } yield result
      case ValidateMode.Sparql => wikibase.sparqlEndpoint match {
        case None => IO.raiseError(NoSparqlEndpoint(wikibase))
        case Some(endpointUri) => {
        RDFAsJenaModel.empty.flatMap(_.use {
          case builder => for {
            rdf <- Endpoint.fromString(endpointUri.toString)
            rdfPrefixMap <- rdf.getPrefixMap
            fixedShapeMap <- ShapeMap.fixShapeMap(shapeMap, rdf, rdfPrefixMap, schema.prefixMap)
            r <- Validator.validate(resolvedSchema, fixedShapeMap, rdf, builder, verbose.toBoolean )           
          } yield r
        })
       }
      }
      case ValidateMode.Cached => wikibase.sparqlEndpoint match {
        case None => IO.raiseError(NoSparqlEndpoint(wikibase))
        case Some(endpointUri) => {
         for {
          res1 <- WikibaseRDF.fromEndpoint(IRI(endpointUri.toString), WikibaseRDF.wikidataPrefixMap)
          res2 <- RDFAsJenaModel.empty
          result <- (res1,res2).tupled.use {
            case (rdf, builder) => for {
             rdfPrefixMap <- rdf.getPrefixMap
             fixedShapeMap <- ShapeMap.fixShapeMap(shapeMap, rdf, rdfPrefixMap, schema.prefixMap)
              r <- Validator.validate(resolvedSchema, fixedShapeMap, rdf, builder, verbose.toBoolean )
           } yield r
          }
         } yield result
        }
       }
    }
    resultShapeMap <- r.toResultShapeMap
    _ <- info(s"End of ShEx-S validation", verbose)
    str <- fromES(resultShapeMap.serialize(resultFormat.name).leftMap(err => s"Error serializing ${resultShapeMap} with format ${resultFormat.name}: $err")) 
    _ <- IO.println(str)
   } yield ExitCode.Success
 }

 def runShExJena(client: Client[IO], wikibase: Wikibase): IO[ExitCode] = wikibase.entityTemplate match {
   case None => IO.raiseError(ErrorNoTemplate(wikibase))
   case Some(et) => for {
   rdfStr <- wikibase.findEntity(entityId, client, InfoMode.Raw, verbose)
   res1 <- RDFAsJenaModel.fromString(rdfStr, "Turtle", None)
   res2 <- RDFAsJenaModel.empty
   report <- (res1,res2).tupled.use {
    case (rdf,builder) => for {
      schema <- resolveSchemaStr(client, wikibase, verbose)
      node = et + entityId
      r <- ShExJena.validate(rdf, schema, node, shape)
     } yield r 
   }
   _ <- IO.println(s"Result ShEx-Jena validation") 
   str <- ShExJena.report2Str(report)
   _ <- IO.println(str)
 } yield ExitCode.Success  
 }
}

object Validate {

 val validateCommand: Opts[Validate] =
    Opts.subcommand("validate", "Validate an entity with an entity schema") {
      (EntitySchema.entitySchema, 
       EntityId.entityId, 
       ShapeOpt.shape, 
       ShExEngine.shexEngine, 
       ValidateMode.validateMode,
       ResultFormat.resultFormat,       
       Wikibase.wikibase, 
       WikibasesPath.path,
       Verbose.verbose,
       ).mapN(Validate.apply)
 }
 
}
