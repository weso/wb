package es.weso.wb

import com.monovore.decline._
import cats.effect._
import cats.data.Validated._
import cats.kernel.Order
import cats.implicits._
import cats.data.Validated
import org.http4s.MediaType

sealed abstract class QueryResultFormat {
    val name: String
    val mimeType: MediaType
}

object QueryResultFormat {
  case object AsciiTable  extends QueryResultFormat { 
    val name = "Table" 
    val mimeType = MediaType.application.json 
  }
  case object JSON        extends QueryResultFormat { 
   val name = "JSON" 
   val mimeType = MediaType.application.json
  } 
  case object XML         extends QueryResultFormat { 
   val name = "XML" 
   val mimeType = MediaType.application.`sparql-results+xml` // "application/sparql-results+xml"
  }

  private lazy val availableFormats = List(AsciiTable, JSON, XML)
  private lazy val defaultFormat = availableFormats.head
  private lazy val availableFormatsNames = availableFormats.map(_.name)
  private lazy val availableFormatsStr = availableFormatsNames.mkString("|")

  val queryResultFormat: Opts[QueryResultFormat] = 
    Opts.option[String]("result-format", s"Result format. Default=${defaultFormat}. Values=${availableFormatsStr}")
    .mapValidated(s => 
     availableFormats.find(_.name.toLowerCase == s.toLowerCase) match {
      case None => Validated.invalidNel(s"Error obtaining schema format. Available values = ${availableFormatsStr}") 
      case Some(sf) => Validated.valid(sf)
    }).withDefault(defaultFormat)

}
  
