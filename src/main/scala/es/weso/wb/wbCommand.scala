package es.weso.wb
import cats.effect._

abstract class WBCommand {
    def run(ctx: Context): IO[ExitCode]
}