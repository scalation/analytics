package apps.analytics

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser
import org.semanticweb.HermiT.{Reasoner => HermiTReasoner}
import org.semanticweb.owlapi.expression.{OWLEntityChecker, ShortFormEntityChecker}
import org.semanticweb.owlapi.model.OWLDataFactory
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory
import org.semanticweb.owlapi.util.{BidirectionalShortFormProviderAdapter, QNameShortFormProvider}
import uk.ac.manchester.cs.jfact.JFactFactory

import scala.collection.JavaConversions._

object ModelClassifier
{

} // ModelClassifier

/** The `ModelClassifierConsistencyTest` object contains some tests for
 *  checking the consistency of the ontology.
 */
object ModelClassifierConsistencyTest extends App
{
    val (manager, ontology) = AnalyticsOntology.loadRemote ()
    
    val hreasoner = (new HermiTReasoner.ReasonerFactory()).createReasoner (ontology);
    val jreasoner = (new JFactFactory()).createReasoner (ontology)

    println ("Ontology is consistent?")
    println (" - HermiT: " + hreasoner.isConsistent ())
    println (" -  JFact: " + jreasoner.isConsistent ())

} // ModelClassifierConsistencyTest

object ModelClassifierInferenceTest extends App
{
    val (manager, ontology) = AnalyticsOntology.loadLocal()
    val dataFactory: OWLDataFactory = manager.getOWLDataFactory

    // Short form provider using qname prefixes. e.g., owl:Thing, analytics:Model etc.
    val sfProvıder = new BidirectionalShortFormProviderAdapter(manager, ontology.getImportsClosure(), new QNameShortFormProvider())

    // Hermit reasoner used for retrieving the inferred axioms.
    val hreasoner = (new HermiTReasoner.ReasonerFactory()).createReasoner (ontology)

    // Structural Reasoner for querying only from the asserted axioms. No inference
    val reasoner = (new StructuralReasonerFactory()).createReasoner(ontology)

    val classExpressionQuery = "owl:Thing"
    // Set up DL Query Parser.
    // Manchester Syntax Class Expression queries could be issued.

    val parser: ManchesterOWLSyntaxEditorParser = new ManchesterOWLSyntaxEditorParser(dataFactory, classExpressionQuery)
    val entityChecker: OWLEntityChecker = new ShortFormEntityChecker(sfProvıder)
    parser.setDefaultOntology(ontology)
    parser.setOWLEntityChecker(entityChecker)

    val instances = reasoner.getInstances(parser.parseClassExpression, true)

    // Retrieve all generic models which are instances of owl:Thing
    println("Direct instances of owl:Thing")
    for (individual <- instances.getFlattened){
    println("\t" + sfProvıder.getShortForm(individual))
    }
    println()


    // Retrieve inferred types of a model.
    val model = "analytics:GenericModel"

    hreasoner.precomputeInferences()
    val genericModel = ontology.getEntitiesInSignature(sfProvıder.getEntity(model).getIRI).iterator().next().asOWLNamedIndividual()


    println("Direct Inferred types of:" + sfProvıder.getShortForm(genericModel))
    //Only retrieve direct inferred types.
    val directTypes = hreasoner.getTypes(genericModel, true) // only get direct inferred types
    for (modelType <- directTypes.getFlattened){
        println("\t" + sfProvıder.getShortForm(modelType))
    }

    println("\nAll (direct + indirect) inferred types of:" + sfProvıder.getShortForm(genericModel))

    //Retrieve Both direct and indirect inferred types.
    val allTypes = hreasoner.getTypes(genericModel, false)
    for (modelType <- allTypes.getFlattened){
        println("\t" + sfProvıder.getShortForm(modelType))
    }

}