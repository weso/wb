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
 outFormat: Option[SchemaFormat], 
 infoMode: InfoMode,
 wikibaseRef: WikibaseRef,
 wikibasesPath: WikibasesPath,
 verbose:  VerboseLevel) extends WBCommand {

  override def run(ctx: Context): IO[ExitCode] = for {
    wikibase <- Wikibase.getWikibase(wikibaseRef, wikibasesPath.path)
    r <- entityId match {
     case sid: EntitySchemaId => for {
      result <- outFormat match {
         case None => wikibase.findSchemaStr(sid.toString, ctx.client, verbose)
         case Some(schemaFormat) => for {
           schema <- wikibase.findSchema(sid.toString, ctx.client, verbose)
           result <- RDFAsJenaModel.empty.flatMap(_.use(builder => 
                       Schema.serialize(schema, schemaFormat.name, None, builder)))
         } yield result
      }
      _ <- IO.println(result)
     } yield ExitCode.Success
     case _: PropertyId | _: ItemId | _ :LexemeId => for {
       result <- wikibase.findEntity(entityId, ctx.client, infoMode, verbose)
       _ <- IO.println(s"Entity: ${entityId}\n$result")
     } yield ExitCode.Success
    }
  } yield r
}

object Info {

 val infoCommand: Opts[Info] =
    Opts.subcommand("info", "Get info about entity") {
      (EntityId.entityId, 
       SchemaFormat.schemaFormat, 
       InfoMode.infoMode, 
       Wikibase.wikibase, 
       WikibasesPath.path,
       Verbose.verbose
      ).mapN(Info.apply)
 }

 
}
