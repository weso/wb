package es.weso.wb
import cats._
import cats.implicits._
import cats.effect._
import com.monovore.decline._
import org.http4s._
import org.http4s.client._
import es.weso.rdf.jena._
import es.weso.shex._
import es.weso.shapemaps.ShapeMap
import es.weso.shex.validator.Validator
import es.weso.utils.IOUtils.fromES

case class Validate(
  schemaId: EntitySchemaId, 
  entityId: EntityId, 
  wikibase: Wikibase, 
  shape: Option[String],
  verbose: Boolean) {
    def run(client: Client[IO]): IO[ExitCode] = { 
        val shapeMapStr = "<" + wikibase.entityTemplate + entityId + ">@" + (shape match {
            case None => "start"
            case Some(s) => s
        })
        for {
         schema <- wikibase.findSchema(schemaId.toString, client)
         rdfStr <- wikibase.findEntity(entityId, client)
         res1 <- RDFAsJenaModel.fromString(rdfStr, "Turtle",None)
         res2 <- RDFAsJenaModel.empty
         result <- (res1,res2).tupled.use {
             case (rdf,builder) => for {
              rdfPrefixMap <- rdf.getPrefixMap
              resolvedSchema <- ResolvedSchema.resolve(schema,None)
              shapeMap <- fromES(ShapeMap.fromCompact(shapeMapStr, schema.base, schema.prefixMap).leftMap(_.toList.mkString("\n")))
              fixedShapeMap <- ShapeMap.fixShapeMap(shapeMap, rdf, rdfPrefixMap, schema.prefixMap)
              r <- Validator.validate(resolvedSchema, fixedShapeMap, rdf, builder, verbose)
              resultShapeMap <- r.toResultShapeMap
             } yield resultShapeMap
         } 
         _ <- IO.println(s"Validation result:\n${result.toJson.spaces2}")
        } yield ExitCode.Success
    }
}

object Validate {

 val validateCommand: Opts[Validate] =
    Opts.subcommand("validate", "Validate an entity with an entity schema") {
      (EntityId.entitySchemaId, EntityId.entityId, Wikibase.wikibase, ShapeOpt.shape, Verbose.verbose).mapN(Validate.apply)
 }

 
}
