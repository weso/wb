package es.weso.wb
import com.monovore.decline._
import cats.effect._
import cats.data.Validated._
import cats.kernel.Order
import cats.implicits._
import es.weso.utils.VerboseLevel
import es.weso.utils.VerboseLevel._

/*sealed abstract class VerboseLevel {
  val level: Int

  def toBoolean: Boolean = level > 0
}*/

object Verbose {
  //case object Nothing     extends VerboseLevel { val level = 0 }
  //case object InfoLevel   extends VerboseLevel { val level = 10 } 
  //case object AllMessages extends VerboseLevel { val level = 20 }

  val verbose: Opts[VerboseLevel] = Opts.option[String]("verbose", "verbose level. 0 = nothing, 1 = info msgs, 2 = all msgs")
    .mapValidated { n => n.toLowerCase match {
       case "0" => valid(Nothing)
       case "1" => valid(Info)
       case "2" => valid(Debug)
       case "nothing" => valid(Nothing)
       case "info" => valid(Info)
       case "all" => valid(Debug)
       // case _ => invalidNel[String,VerboseLebel]("Verbose level must be 0-2 or nothing|info|all")
     }
  }.withDefault(Nothing)

  def info(msg: String, verbose: VerboseLevel): IO[Unit] = 
    if (verbose > Nothing) {
     IO.println(msg)
    } else 
     IO.pure(())

  def infoVerbose(msg: String, verbose: VerboseLevel): IO[Unit] = 
    if (verbose > Info) {
     IO.println(msg)
    } else 
     IO.pure(())     

  implicit val OrderVerbose: Order[VerboseLevel] = new Order[VerboseLevel] {
    def compare(x: VerboseLevel, y: VerboseLevel) = 
      Order[Int].compare(x.level,y.level)
  }

}
  
