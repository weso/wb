package es.weso.wb
import cats._
import cats.data._
import cats.implicits._
import com.monovore.decline._

sealed abstract class SchemaFormat {
    def name: String
}

object SchemaFormat {
  private case object ShExC extends SchemaFormat { val name = "ShExC" }
  private case object ShExJ extends SchemaFormat { val name = "ShExJ" }
  private case object Raw extends SchemaFormat { val name = "RAW" }

  lazy val availableSchemaFormats = List(ShExC, ShExJ)
  lazy val defaultSchemaFormat = availableSchemaFormats.head
  lazy val availableSchemaFormatsStr = availableSchemaFormats.map(_.name).mkString(",")

  val schemaFormat: Opts[Option[SchemaFormat]] = 
      Opts.option[String]("schemaFormat", 
       metavar = "format", 
       help = s"Schema format, default = ($defaultSchemaFormat). Possible values = ($availableSchemaFormatsStr)")
       .mapValidated(s => 
         availableSchemaFormats.find(_.name == s) match {
          case None => Validated.invalidNel(s"Error obtaining wikibase. Available values = ${availableSchemaFormatsStr}") 
          case Some(sf) => Validated.valid(Some(sf))
      }).withDefault(None)

}