package es.weso.wb 
import com.monovore.decline._
import cats._
import cats.implicits._
import org.http4s._

sealed abstract class EntityId(letter: Char) {
    val value: Int
    val name: String = s"$letter$value"
    override def toString: String = name
}
case class ItemId(value: Int) extends EntityId('Q') 
case class PropertyId(value: Int) extends EntityId('P') 
case class LexemeId(value: Int) extends EntityId('L')
case class EntitySchemaId(value: Int) extends EntityId('E')


object EntityId {

  // private case class EntitySchemaId(value: Int) extends EntityId('E')

  lazy val entityId: Opts[EntityId] = Opts.option[EntityId](
      long = "entity",
      metavar = "id",
      short = "e",
      help = "Entity Id, examples: Q42, P31"
  )

  lazy val entitySchemaId: Opts[EntitySchemaId] = 
    Opts.option[String](
     "schema", 
     short = "s",
     metavar = "E..",
     help="Entity schema Id, example: E42")
    .mapValidated(s => parseEntitySchema(s) 
     match {
        case Some(v) => v.validNel[String]
        case None    => "schema Id must begin with E followed by numbers (example: E42).".invalidNel
     }
    )  
 
  private val EntityPat = "^(.)(\\d+)$".r  
  private val EntitySchemaPat = "^E(\\d+)$".r  


  def parse(s: String): Option[EntityId] = 
      s match {
          case EntityPat("Q",ns) => Some(ItemId(ns.toInt))
          case EntityPat("P",ns) => Some(PropertyId(ns.toInt))
          case EntityPat("L",ns) => Some(LexemeId(ns.toInt))
          case EntityPat("E",ns) => Some(EntitySchemaId(ns.toInt))
          case _ => None
      }

  def parseEntitySchema(s: String): Option[EntitySchemaId] = 
    s match {
      case EntitySchemaPat(ns) => Some(EntitySchemaId(ns.toInt))
      case _ => None
    }


  implicit val ArgumentEntityId: Argument[EntityId] =
    Argument.from("entityId") { s =>
      parse(s) match {
        case Some(v) => v.validNel
        case None    => "schemaId must begin with P, Q, L or E followed by numbers (example: Q42).".invalidNel
      }
    }
}