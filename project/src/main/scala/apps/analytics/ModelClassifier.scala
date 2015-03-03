package apps.analytics

import org.semanticweb.HermiT.{ Reasoner => HermiTReasoner }

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
