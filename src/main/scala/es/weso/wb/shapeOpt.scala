package es.weso.wb
import com.monovore.decline._

object ShapeOpt {
  val shape = Opts.option[String]("shape", "shape in entity schema").orNone
}