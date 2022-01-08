package es.weso.wb
import com.monovore.decline._
import cats.effect._
import cats.data.Validated._
import java.nio.file.Path
import java.nio.file.Paths

case class WikibasesPath(path: Path) 

object WikibasesPath{

  val name = "wikibases.json"
  val defaultPath: Path = Paths.get(System.getProperty("user.dir") + "/" + name)
  
  val path : Opts[WikibasesPath] = Opts.option[Path](
     "wikibases-file", 
     metavar="file",
     help = s"Wikibases configuration file. Default = [user.dir]/$name")
    .withDefault(defaultPath)
    .map(WikibasesPath.apply)
    
}