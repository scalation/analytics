package apps.analytics

import org.semanticweb.HermiT.{ Reasoner => HermiTReasoner }
import org.semanticweb.owlapi.model.{ IRI, OWLOntology, OWLOntologyManager }
import org.semanticweb.owlapi.apibinding.OWLManager

object ModelClassifier
{

} // ModelClassifier

/** The `ModelClassifierTest` object contains some tests for the
 *  `ModelClassifier` class.
 */ 
object ModelClassifierTest extends App
{
    val (manager, ontology) = AnalyticsOntology.loadRemote ()
    
    val hreasoner = (new HermiTReasoner.ReasonerFactory()).createReasoner (ontology);

    println ("Ontology is consistent?")
    println (" - HermiT: " + hreasoner.isConsistent ())

} // ModelClassifierTest
