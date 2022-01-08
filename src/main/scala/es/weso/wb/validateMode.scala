package es.weso.wb
import com.monovore.decline._
import cats.effect._
import cats.data.Validated._

sealed abstract class ValidateMode {
    val name: String
}

object ValidateMode {

  case object Simple extends ValidateMode { val name = "Simple"}
  case object Sparql extends ValidateMode { val name = "Sparql"}
  case object Cached extends ValidateMode { val name = "Cached"}

  private lazy val availableValidateModes: List[ValidateMode] = List(Simple, Sparql, Cached)
  private lazy val defaultValidateMode = availableValidateModes.head
  private lazy val availableValidateModesNames = availableValidateModes.map(_.name)
  private lazy val availableValidateModesStr = availableValidateModes.map(_.name).mkString("|")

  val validateMode : Opts[ValidateMode] = Opts.option[String](
    "mode", 
    metavar="mode",
    help = s"Validate mode, default = $defaultValidateMode. Possible values = $availableValidateModesStr")
       .mapValidated(s => 
         availableValidateModes.find(_.name.toLowerCase == s.toLowerCase) match {
          case None => invalidNel(s"Error obtaining validate mode. Available values = ${availableValidateModesStr}") 
          case Some(v) => valid(v)
      }).withDefault(defaultValidateMode)
    

}