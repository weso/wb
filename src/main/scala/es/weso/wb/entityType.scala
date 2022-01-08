package es.weso.wb
import com.monovore.decline._
import cats.effect._
import cats.data.Validated._

sealed abstract class EntityType {
    val name: String
}

object EntityType {

  // The list of entity types is taken from: https://www.wikidata.org/w/api.php?action=help&modules=wbsearchentities
  case object Item extends EntityType { val name = "item"}
  case object Property extends EntityType { val name = "property"}
  case object Lexeme extends EntityType { val name = "lexeme"}
  case object Sense extends EntityType { val name = "sense"}
  case object Form extends EntityType { val name = "form"}
  // TODO: It seems it is not yet possible to search entity schemas :(

  private lazy val availableEntityTypes: List[EntityType] = List(Item, Property, Lexeme)
  private lazy val defaultEntityType = availableEntityTypes.head
  private lazy val availableEntityTypesNames = availableEntityTypes.map(_.name)
  private lazy val availableEntityTypesStr = availableEntityTypesNames.mkString("|")

  val entityType : Opts[EntityType] = Opts.option[String](
    "type", 
    short = "t",
    help = s"Entity type, default = $defaultEntityType. Possible values = $availableEntityTypesStr")
       .mapValidated(s => 
         availableEntityTypes.find(_.name.toLowerCase == s.toLowerCase) match {
          case None => invalidNel(s"Error obtaining entity type. Available values = ${availableEntityTypesStr}") 
          case Some(v) => valid(v)
      }).withDefault(defaultEntityType)

}