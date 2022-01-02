package es.weso.wb
import com.monovore.decline._
import cats.effect._
import cats.data.Validated._

sealed abstract class InfoMode {
    val name: String
}

object InfoMode {

  case object Raw extends InfoMode { val name = "Raw"}
  case object Out extends InfoMode { val name = "Out"}

  private lazy val availableInfoModes: List[InfoMode] = List(Out, Raw)
  private lazy val defaultInfoMode = availableInfoModes.head
  private lazy val availableInfoModesNames = availableInfoModes.map(_.name)
  private lazy val availableInfoModesStr = availableInfoModes.map(_.name).mkString("|")

  val infoMode : Opts[InfoMode] = Opts.option[String](
    "mode", 
    metavar="mode",
    help = s"Info mode, default = $defaultInfoMode. Possible values = $availableInfoModesStr")
       .mapValidated(s => 
         availableInfoModes.find(_.name.toLowerCase == s.toLowerCase) match {
          case None => invalidNel(s"Error obtaining info mode. Available values = ${availableInfoModesStr}") 
          case Some(v) => valid(v)
      }).withDefault(defaultInfoMode)
    

}