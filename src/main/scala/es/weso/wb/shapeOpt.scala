package es.weso.wb
import com.monovore.decline._

object ShapeOpt {
  val shape = Opts.option[String](
   "shape", 
   "Shape to validate in entity schema (if not specified, it uses Start shape)")
  .orNone
}