package apps.analytics

import org.semanticweb.HermiT.Reasoner
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.apibinding.OWLManager

object ModelClassifier
{


} // ModelClassifier

object ModelClassifierTest extends App
{
    val iri      = IRI.create ("https://raw.githubusercontent.com/scalation/analytics/master/analytics.owl")
    val manager  = OWLManager.createOWLOntologyManager ()
    val ontology = manager.loadOntologyFromOntologyDocument (iri)
    val reasoner = (new Reasoner.ReasonerFactory()).createReasoner (ontology);

    println ("Ontology is consistent: " + reasoner.isConsistent ())

} // ModelClassifierTest
