package apps.analytics

import org.semanticweb.HermiT.{ Reasoner => HermiTReasoner }
import uk.ac.manchester.cs.jfact.JFactFactory

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
    val jreasoner = (new JFactFactory()).createReasoner (ontology)

    println ("Ontology is consistent?")
    println (" - HermiT: " + hreasoner.isConsistent ())
    println (" -  JFact: " + jreasoner.isConsistent ())

} // ModelClassifierTest
