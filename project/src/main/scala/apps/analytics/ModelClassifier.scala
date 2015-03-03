package apps.analytics

import java.io.File

import org.semanticweb.HermiT.{ Reasoner => HermiTReasoner }
import org.semanticweb.owlapi.model.{ IRI, OWLOntology, OWLOntologyManager }
import org.semanticweb.owlapi.apibinding.OWLManager

object ModelClassifier
{
    val remoteIRI = IRI.create ("https://raw.githubusercontent.com/scalation/analytics/master/analytics.owl")
    val localIRI  = IRI.create ((new File ("../analytics.owl")).toURI ())

    private def load (iri: IRI): Tuple2[OWLOntologyManager, OWLOntology] =
    {
        val manager  = OWLManager.createOWLOntologyManager ()
        val ontology = manager.loadOntologyFromOntologyDocument (iri)
        (manager, ontology)
    } // load

    def loadLocal ()  = load (localIRI)
    def loadRemote () = load (remoteIRI)

} // ModelClassifier

object ModelClassifierTest extends App
{
    val (manager, ontology) = ModelClassifier.loadRemote ()
    
    val hreasoner = (new HermiTReasoner.ReasonerFactory()).createReasoner (ontology);

    println ("Ontology is consistent?")
    println (" - HermiT: " + hreasoner.isConsistent ())

} // ModelClassifierTest
