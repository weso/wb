package es.weso.wb
import cats._
import cats.data._
import cats.implicits._
import com.monovore.decline._
import java.nio.file.Path

sealed abstract class EntitySchema

object EntitySchema {

  case class EntitySchemaRef(schemaId: EntitySchemaId) extends EntitySchema
  case class EntitySchemaPath(path: Path) extends EntitySchema

  val schemaPath: Opts[Path] = 
    Opts.option[Path](
      "schema-file", 
      metavar = "file",
      help="File that contains entity schema"
    )

  val entitySchema: Opts[EntitySchema] = 
    (EntityId.entitySchemaId orElse
     schemaPath).map {
        case es: EntitySchemaId => EntitySchemaRef(es)
        case path: Path => EntitySchemaPath(path)
  }

}