package es.weso.wb
import com.monovore.decline._

object Verbose {
  val verbose = Opts.flag("verbose", "show more info").orFalse
}