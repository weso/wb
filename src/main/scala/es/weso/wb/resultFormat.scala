package es.weso.wb
import cats.data.Validated._
import com.monovore.decline._

sealed abstract class ResultFormat {
    def name: String
}

object ResultFormat {

  case object JSON extends ResultFormat { val name = "JSON" }
  case object Compact extends ResultFormat { val name = "Compact" }
  case object Details extends ResultFormat { val name = "Details" }

  lazy val availableFormats = List(Details, Compact, JSON)
  lazy val defaultFormat = availableFormats.head
  lazy val availableFormatsStrs = availableFormats.map(_.name)
  lazy val availableFormatsStr = availableFormatsStrs.mkString("|")

  val resultFormat: Opts[ResultFormat] = Opts.option[String](
   "result-format", 
   metavar = "format",
   help = s"result-format, default = $defaultFormat, values=$availableFormatsStr")
  .mapValidated(s => 
    availableFormats.find(_.name.toLowerCase == s.toLowerCase) match {
        case None => invalidNel(s"Error obtaining result-format $s, value should be: ${availableFormatsStr}")
        case Some(rf) => valid(rf)
   }).withDefault(defaultFormat)
}