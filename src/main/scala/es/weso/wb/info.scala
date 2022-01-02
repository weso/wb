package es.weso.wb
import cats._
import cats.implicits._
import cats.effect._
import com.monovore.decline._
import org.http4s._
import org.http4s.client._
import es.weso.rdf.jena._
import es.weso.shex._
import es.weso.wb.Verbose._

case class Info(
 entityId: EntityId, 
 wikibase: Wikibase, 
 outFormat: Option[SchemaFormat], 
 infoMode: InfoMode,
 verbose:  VerboseLevel) {

  def run(client: Client[IO]): IO[ExitCode] = entityId match {
     case sid: EntitySchemaId => for {
      result <- outFormat match {
         case None => wikibase.findSchemaStr(sid.toString, client, verbose)
         case Some(schemaFormat) => for {
           schema <- wikibase.findSchema(sid.toString, client, verbose)
           result <- RDFAsJenaModel.empty.flatMap(_.use(builder => 
                       Schema.serialize(schema, schemaFormat.name, None, builder)))
         } yield result
      }
      _ <- IO.println(result)
     } yield ExitCode.Success

     case _: PropertyId | _: ItemId | _ :LexemeId => for {
       result <- wikibase.findEntity(entityId, client, infoMode, verbose)
       _ <- IO.println(s"Entity: ${entityId}\n$result")
     } yield ExitCode.Success

    }
}

object Info {

 val infoCommand: Opts[Info] =
    Opts.subcommand("info", "Get info about entity") {
      (EntityId.entityId, 
       Wikibase.wikibase, 
       SchemaFormat.schemaFormat, 
       InfoMode.infoMode, 
       Verbose.verbose
      ).mapN(Info.apply)
 }

 
}
