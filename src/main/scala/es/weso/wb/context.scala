package es.weso.wb

import cats.effect._
import org.http4s.client.Client

case class Context(
 client: Client[IO],
// wikibase: Wikibase
)