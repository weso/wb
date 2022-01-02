package es.weso.shexjena

import cats.effect._
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.shex._
import org.apache.jena.shex.sys.ShexLib
import org.apache.jena.vocabulary.RDF
import es.weso.rdf.jena.RDFAsJenaModel
import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
import java.util.function.Consumer

object ShExJena {
 def validate(
  rdf: RDFAsJenaModel, 
  shexStr: String, 
  nodeStr: String, 
  shape: Option[String]
  ): IO[ShexReport] = {

    val shexSchema: ShexSchema = Shex.schemaFromString(shexStr)

    val node: Node = NodeFactory.createURI(nodeStr)

    val shapeRef: Node = shape match {
        case None => shexSchema.getStart.getLabel
        case Some(s) => NodeFactory.createURI(s)
    }

    val shapeMap: ShexMap = ShexMap.record(node, shapeRef)

    for {
      model <- rdf.modelRef.get
      graph = model.getGraph
      validator = ShexValidator.get()
      report = validator.validate(graph,shexSchema,shapeMap)
    } yield report 
  }

  def report2Str(report: ShexReport): IO[String] = IO {
    val builder = new StringBuilder()
    if (report.hasReports) {
      report.forEachReport(consumer)
    } else {
      builder ++= "Empty report"
    }

    def consumer: Consumer[ShexRecord] = (r: ShexRecord) => {
      builder ++= s"${r.strTarget} :: Status = ${showStatus(r)}"
    }

    def showStatus(r: ShexRecord): String = {
      s"${r.status} ${if (r.reason == null) "" else s", Reason: ${r.reason}"}"
    }

    builder.toString
  }
}