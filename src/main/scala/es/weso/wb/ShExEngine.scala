package es.weso.wb
import cats.data._
import com.monovore.decline._

sealed abstract class ShExEngine {
    val name: String
}

object ShExEngine {

  case object ShExS extends ShExEngine { val name = "ShExS" }
  case object Jena  extends ShExEngine { val name = "Jena" }

  val availableShExEngines = List(ShExS, Jena)
  val defaultShExEngine = availableShExEngines.head
  val shexEnginesStr = availableShExEngines.map(_.name).mkString(",")

  val shexEngine: Opts[ShExEngine] = 
      Opts.option[String](
       "shex-engine", 
       short = "x",
       metavar="engine", 
       help = s"ShEx engine. Available engines: ${shexEnginesStr}")
      .mapValidated(s => 
       availableShExEngines.find(_.name.toLowerCase == s.toLowerCase) match {
          case None => Validated.invalidNel(s"Error obtaining schema format. Available values = ${shexEnginesStr}") 
          case Some(e) => Validated.valid(e)
      }).withDefault(defaultShExEngine)
    
}