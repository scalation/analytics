package apps.analytics

import java.util

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser
import org.semanticweb.HermiT.{Reasoner => HermiTReasoner}
import org.semanticweb.owlapi.expression.{OWLEntityChecker, ShortFormEntityChecker}
import org.semanticweb.owlapi.model.{IRI, OWLAxiom, OWLDataFactory}
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
    val ontology = AnalyticsOntologyFactory.loadRemote ()
    
    val hreasoner = (new HermiTReasoner.ReasonerFactory()).createReasoner (ontology);
    val jreasoner = (new JFactFactory()).createReasoner (ontology)

    println ("Ontology is consistent?")
    println (" - HermiT: " + hreasoner.isConsistent ())
    println (" -  JFact: " + jreasoner.isConsistent ())

} // ModelClassifierConsistencyTest

object ModelClassifierInferenceTest extends App
{
    val ontology = AnalyticsOntologyFactory.loadLocal()
    val dataFactory: OWLDataFactory = ontology.getOWLOntologyManager.getOWLDataFactory

    // Short form provider using qname prefixes. e.g., owl:Thing, analytics:Model etc.
    val sfProvider = new BidirectionalShortFormProviderAdapter(ontology.getOWLOntologyManager, ontology.getImportsClosure(), new QNameShortFormProvider())

    // Hermit reasoner used for retrieving the inferred axioms.
    val hreasoner = (new HermiTReasoner.ReasonerFactory()).createReasoner (ontology)

    // Structural Reasoner for querying only from the asserted axioms. No inference
    val reasoner = (new StructuralReasonerFactory()).createReasoner(ontology)

    val classExpressionQuery = "owl:Thing"
    // Set up DL Query Parser.
    // Manchester Syntax Class Expression queries could be issued.

    val parser: ManchesterOWLSyntaxEditorParser = new ManchesterOWLSyntaxEditorParser(dataFactory, classExpressionQuery)
    val entityChecker: OWLEntityChecker = new ShortFormEntityChecker(sfProvider)
    parser.setDefaultOntology(ontology)
    parser.setOWLEntityChecker(entityChecker)

    val instances = reasoner.getInstances(parser.parseClassExpression, true)

    // Retrieve all generic models which are instances of owl:Thing
    println("Direct instances of owl:Thing")
    for (individual <- instances.getFlattened){
    println("\t" + sfProvider.getShortForm(individual))
    }
    println()


    // Retrieve inferred types of a model.
    val model = "analytics:GenericModel"

    hreasoner.precomputeInferences()
    val genericModel = ontology.getEntitiesInSignature(sfProvider.getEntity(model).getIRI).iterator().next().asOWLNamedIndividual()


    println("Direct Inferred types of:" + sfProvider.getShortForm(genericModel))
    //Only retrieve direct inferred types.
    val directTypes = hreasoner.getTypes(genericModel, true) // only get direct inferred types
    for (modelType <- directTypes.getFlattened){
        println("\t" + sfProvider.getShortForm(modelType))
    }

    println("\nAll (direct + indirect) inferred types of:" + sfProvider.getShortForm(genericModel))

    //Retrieve Both direct and indirect inferred types.
    val allTypes = hreasoner.getTypes(genericModel, false) // get all inferred types
    for (modelType <- allTypes.getFlattened){
        println("\t" + sfProvider.getShortForm(modelType))
    }

}

object ModelClassifierInferenceTest2 extends App
{
    val ontology = new AnalyticsOntology(AnalyticsOntologyFactory.loadRemote())
    val model = ontology.retrieveIndividual("analytics:GenericModel")

    println("Direct Inferred types of:\t" + ontology.getShortForm(model))
    //Only retrieve direct inferred types.
    val modelTypes = ontology.retrieveTypes(model)
    for (modelType <- modelTypes){
        println("\t" + ontology.getShortForm(modelType))
    }

    println("All Inferred types of:\t" + ontology.getShortForm(model))
    //Retrieve both direct and indirect inferred types.
    val modelTypesAll = ontology.retrieveTypes(model,false)
    for (modelType <- modelTypesAll){
        println("\t" + ontology.getShortForm(modelType))
    }
}

object ModelClassifierCreationTest extends App
{
    val ontology = AnalyticsOntologyFactory.loadRemote()
    val manager = ontology.getOWLOntologyManager
    val factory = manager.getOWLDataFactory

    // Structural Reasoner for querying only from the asserted axioms. No inference
    //val reasoner = (new StructuralReasonerFactory()).createReasoner(ontology)
    val reasoner = new HermiTReasoner.ReasonerFactory().createNonBufferingReasoner(ontology)

    val sfProvider = new BidirectionalShortFormProviderAdapter(ontology.getOWLOntologyManager, ontology.getImportsClosure(), new QNameShortFormProvider())
    val baseIRI = ontology.getOntologyID.getOntologyIRI

    val changes = new util.HashSet[OWLAxiom]

    val variableClass = factory.getOWLClass(sfProvider.getEntity("analytics:Variable").getIRI)
    val modelClass = factory.getOWLClass(sfProvider.getEntity("analytics:Model").getIRI)
    val continuousVariableType = factory.getOWLNamedIndividual( sfProvider.getEntity("analytics:Non_Negative_Variable_Type").getIRI)
    val identityLinkFunction = factory.getOWLNamedIndividual( sfProvider.getEntity("analytics:Identity_Function").getIRI)

    val hasVariable = factory.getOWLObjectProperty( IRI.create(baseIRI + "#hasVariable"))
    val hasResponseVariable = factory.getOWLObjectProperty( IRI.create(baseIRI + "#hasResponseVariable"))
    val hasPredictorVariable = factory.getOWLObjectProperty( IRI.create(baseIRI + "#hasPredictorVariable"))
    val hasVariableType = factory.getOWLObjectProperty( IRI.create(baseIRI + "#hasVariableType"))
    val hasResidualDistribution = factory.getOWLObjectProperty( IRI.create(baseIRI + "#hasResidualDistribution"))
    val hasLinkFunction = factory.getOWLObjectProperty( IRI.create(baseIRI + "#hasLinkFunction"))

    //Create a new model
    val model = factory.getOWLNamedIndividual( IRI.create( baseIRI + "#testModel"))

    val classExpressionAxiom = factory.getOWLClassAssertionAxiom(modelClass, model)
    val linkFunctionAxiom = factory.getOWLObjectPropertyAssertionAxiom(hasLinkFunction,model,identityLinkFunction)

    changes.add( classExpressionAxiom )
    changes.add( linkFunctionAxiom )

    //Create mpg variable (response) and assert axioms
    val variableMPG = factory.getOWLNamedIndividual( IRI.create( baseIRI + "#variableMPG"))

    val typeAxiom = factory.getOWLClassAssertionAxiom(variableClass,variableMPG)
    val variableTypeAxiom = factory.getOWLObjectPropertyAssertionAxiom(hasVariableType, variableMPG, continuousVariableType)
    val responseVariableAxiom = factory.getOWLObjectPropertyAssertionAxiom(hasResponseVariable,model, variableMPG)

    changes.add(typeAxiom)
    changes.add(variableTypeAxiom)
    changes.add(responseVariableAxiom)


    //Create horsepower variable and assert axioms
    val variableHP = factory.getOWLNamedIndividual( IRI.create( baseIRI + "#variableHP"))

    val hpTypeAxiom = factory.getOWLClassAssertionAxiom(variableClass, variableHP)
    val hpVariableTypeAxiom = factory.getOWLObjectPropertyAssertionAxiom(hasVariableType, variableHP, continuousVariableType)
    val hpPredictorVariableAxiom = factory.getOWLObjectPropertyAssertionAxiom(hasPredictorVariable, model, variableHP)

    changes.add(hpTypeAxiom)
    changes.add(hpVariableTypeAxiom)
    changes.add(hpPredictorVariableAxiom)

    val variableDifferentIndividualsAxiom = factory.getOWLDifferentIndividualsAxiom(variableHP, variableMPG)

    changes.add(variableDifferentIndividualsAxiom)

    val objectOneOf = factory.getOWLObjectOneOf(variableMPG, variableHP)
    val allValuesFromExpression = factory.getOWLObjectAllValuesFrom(hasVariable,objectOneOf)
    val modelRestrictionAxiom = factory.getOWLClassAssertionAxiom(allValuesFromExpression,model)

    changes.add(modelRestrictionAxiom)

    manager.applyChanges(manager.addAxioms(ontology,changes))

    //val residualDistributionAxiom = factory.getOWLObjectPropertyAssertionAxiom(hasResidualDistribution,model,)
//    reasoner.flush()

    println("Model Types:")
    reasoner.precomputeInferences()
    val modelTypes = reasoner.getTypes(model, true).getFlattened
    for (variableType <- modelTypes){
        println("\t" + sfProvider.getShortForm(variableType))
    }


    println("Response Variables of Model:")
    val variableTypes = reasoner.getObjectPropertyValues(model, hasVariable).getFlattened
    for (variableType <- variableTypes){
        println("\t" + sfProvider.getShortForm(variableType))
    }

    //manager.saveOntology(ontology, System.out)


}

object ModelClassifierCreationTest2 extends App
{
    val model = new Model

    val variableMPG = new Variable("MPG", true, VariableTypes.Non_Negative_Continuous)
    val variableHP  = new Variable("Horsepower", false, VariableTypes.Non_Negative_Continuous)

    model.variables += variableMPG
    model.variables += variableHP

    println("Suitable types for the current state of the model")
    for (modelType <- model.getModelTypes){
        println("\t" + modelType)
    }
}