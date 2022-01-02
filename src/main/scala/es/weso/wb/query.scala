package es.weso.wb
import cats._
import cats.implicits._
import cats.effect._
import com.monovore.decline._
import org.http4s._
import org.http4s.client._
import es.weso.rdf.jena._
import java.nio.file.Path
import es.weso.shex.{Path => _, _}
import es.weso.wb.Verbose._

sealed abstract class Query 

object Query {
 case class QueryPath(path: Path) extends Query

 val query: Opts[Query] =
    Opts.option[Path](
      "query-file", 
      metavar = "file", 
      help = "File that contains the SPARQL query"
    ).map { 
      case path: Path => QueryPath(path) 
    }

}
