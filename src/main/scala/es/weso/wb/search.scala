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
import io.circe._
import io.circe.parser._
import org.http4s.circe._
import es.weso.utils.VerboseLevel

/**
 * This is a wrapper on Wikbase API wbsearchentities: https://www.wikidata.org/w/api.php?action=help&modules=wbsearchentities
 **/ 
case class Search(
 name: String, 
 lang: String,
 limit: Int,
 offset: Int,
 entityType: EntityType,
 wikibaseRef: WikibaseRef,
 wikibasesPath: WikibasesPath,
 verbose:  VerboseLevel
 ) extends WBCommand {

  override def run(ctx: Context): IO[ExitCode] = 
   for {
    wikibase <- Wikibase.getWikibase(wikibaseRef, wikibasesPath.path, verbose)
    json <- wikibase.root match {
      case None => IO.raiseError(ErrorNoRoot(wikibase))
      case Some(rootUri) => {
        val request = rootUri.withPath("/w/api.php").
         withQueryParam("action", "wbsearchentities").
         withQueryParam("search", name).
         withQueryParam("language", lang).
         withQueryParam("limit",limit).
         withQueryParam("type",entityType.name).
         withQueryParam("continue",offset).
         withQueryParam("format","json")
        ctx.client.expect[Json](request) 
      }
    }
    _ <- IO.println(json.spaces2)
   } yield ExitCode.Success
}

object Search {

 val defaultLimit = 5
 val defaultOffset = 0
 val name: Opts[String] = Opts.option("name", short = "n", metavar = "...", help = "name to search")
 val lang: Opts[String] = Opts.option("lang", short = "l", help = "language, default = en").withDefault("en")
 val limit: Opts[Int] = Opts.option[Int]("limit", metavar = "number", help = s"Limit number of results, default = $defaultLimit").withDefault(defaultLimit)
 val offset: Opts[Int] = Opts.option[Int]("offset", metavar = "offset", help = s"Offset where to continue search, default = $defaultOffset").withDefault(defaultOffset)

 val search: Opts[Search] =
    Opts.subcommand("search", "Search entities") {
      (name, 
       lang,
       limit, 
       offset,
       EntityType.entityType,
       Wikibase.wikibase,
       WikibasesPath.path,
       Verbose.verbose
      ).mapN(Search.apply)
 }

 
}
