package apps.analytics.dashboard

import java.util

import apps.analytics.dashboard.model.Model
import apps.analytics.dashboard.model.VariableTypes.VariableType
import org.semanticweb.HermiT.{Reasoner => HermiTReasoner}
import org.semanticweb.owlapi.model._
import org.semanticweb.owlapi.util.{BidirectionalShortFormProviderAdapter, QNameShortFormProvider}

import scala.collection.JavaConversions._
import scala.collection.mutable.Set

private class AnalyticsOntology ()
{

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** The OWLOntology instance
      */
    val ontology = AnalyticsOntology.initOntology

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** The Ontology Manager
     */
    val manager = ontology.getOWLOntologyManager

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** The ontology data factory for creating OWLEntity objects
      * (class, individual, axiom, etc.)
      */
    val factory = manager.getOWLDataFactory

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** The base IRI for the Analytics ontology
      */
    val baseIRI = ontology.getOntologyID.getOntologyIRI

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** The short form provider to avoid using the full IRI of an entity 
      * during search. Currently, it is using qname short form provider which 
      * allows forms such as owl:Thing, analytics:Model, etc.
      */
    val sfProvider = new BidirectionalShortFormProviderAdapter(manager, ontology.getImportsClosure(), new QNameShortFormProvider())


    val reasonerFactory = new HermiTReasoner.ReasonerFactory()
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** The reasoner used for inference. Loads HermiT Reasoner by default.
      * This might be configurable in the future.
      */
    val hreasoner = reasonerFactory.createNonBufferingReasoner(ontology)

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Retrieve short form of an entity as a String.  
      *  @param entity the entity for which the short form is required.
      */
    def getShortForm(entity: OWLEntity): String = sfProvider.getShortForm(entity)

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Retrieve the individual identified with the given qualified name.
      *  @param qName  The qualified name of the individual. eg., analytics:GenericModel.
      */
    def retrieveIndividual (qName : String) : OWLNamedIndividual =
    {
        ontology.getEntitiesInSignature(sfProvider.getEntity(qName).getIRI).iterator().next().asOWLNamedIndividual()
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Retrieve the inferred model types for the given individual
      *  @param individual OWLNamedIndividual
      *  @param isDirect indicates whether only directly inferred types
      *                  should be returned
      */
    def retrieveTypes(individual: OWLNamedIndividual, isDirect: Boolean = true) : Set[OWLClass] =
    {
        hreasoner.precomputeInferences()
        hreasoner.getTypes(individual, isDirect).getFlattened
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Retrieve the inferred model types for the given conceptual dataset
      *  @param model The conceptual model representing a dataset
      */
    def retrieveTypes(model: Model): Set[OWLClass] ={
        retrieveTypes(model, true)
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Retrieve the inferred model types for the given conceptual dataset/
      *  @param model The conceptual model representing a dataset
      *  @param isDirect indicates whether only directly inferred types
      *                  should be returned
      */
    def retrieveTypes(model: Model, isDirect: Boolean ) : Set[OWLClass] =
    {
        val changes = new util.HashSet[OWLAxiom]

        val variableClass = factory.getOWLClass(sfProvider.getEntity("analytics:Variable").getIRI)
        //val modelClass = factory.getOWLClass(sfProvider.getEntity("analytics:Model").getIRI)
        //val continuousVariableType = factory.getOWLNamedIndividual( sfProvider.getEntity("analytics:Non_Negative_Variable_Type").getIRI)
        val normalDistribution = factory.getOWLNamedIndividual(IRI.create(baseIRI + "#Normal_Distribution_Instance"))
        //val identityLinkFunction = factory.getOWLNamedIndividual( sfProvider.getEntity("analytics:Identity_Function").getIRI)

        val hasVariable = factory.getOWLObjectProperty( IRI.create(baseIRI + "#hasVariable"))
        val hasResponseVariable = factory.getOWLObjectProperty( IRI.create(baseIRI + "#hasResponseVariable"))
        val hasPredictorVariable = factory.getOWLObjectProperty( IRI.create(baseIRI + "#hasPredictorVariable"))
        val hasVariableType = factory.getOWLObjectProperty( IRI.create(baseIRI + "#hasVariableType"))

        val hasResidualDistribution = factory.getOWLObjectProperty( IRI.create(baseIRI + "#hasResidualDistribution"))
        //val hasLinkFunction = factory.getOWLObjectProperty( IRI.create(baseIRI + "#hasLinkFunction"))

        val isDataIndependent = factory.getOWLDataProperty( IRI.create (baseIRI + "#hasRepeatedObservations"))

        //Create a new individual for this model
        val ontModel = factory.getOWLNamedIndividual( IRI.create( baseIRI + "#" + model.id))

        println("Model ID:" + model.id)
        val classExpressionAxiom = factory.getOWLClassAssertionAxiom(factory.getOWLThing, ontModel)
        //val linkFunctionAxiom = factory.getOWLObjectPropertyAssertionAxiom(hasLinkFunction,ontModel,identityLinkFunction)

        changes.add( classExpressionAxiom )
//        changes.add( linkFunctionAxiom )

        //Create mpg variable (response) and assert axioms
        val variables = Set[OWLIndividual]()
        for (variable <- model.variables){
            val ontVariable = factory.getOWLNamedIndividual( IRI.create( baseIRI + "#" + variable.id))
            val typeAxiom = factory.getOWLClassAssertionAxiom(variableClass, ontVariable)
            changes.add(typeAxiom)

            if (variable.fxVariableType != null)
            {
                val variableTypeAxiom = factory.getOWLObjectPropertyAssertionAxiom(hasVariableType, ontVariable, getVariableType(variable.variableType))
                changes.add(variableTypeAxiom)
            }

            if (variable.isResponse){
                val responseVariableAxiom = factory.getOWLObjectPropertyAssertionAxiom(hasResponseVariable,ontModel, ontVariable)
                changes.add(responseVariableAxiom)
            }else{
                val predictorVariableAxiom = factory.getOWLObjectPropertyAssertionAxiom(hasPredictorVariable, ontModel, ontVariable)
                changes.add(predictorVariableAxiom)
            }
            variables += ontVariable
        }

        val variableDifferentIndividualsAxiom = factory.getOWLDifferentIndividualsAxiom(variables)
        changes.add(variableDifferentIndividualsAxiom)

        val objectOneOfVariables = factory.getOWLObjectOneOf(variables)
        val allValuesFromExpressionVariables = factory.getOWLObjectAllValuesFrom(hasVariable, objectOneOfVariables)
        val variablesClosureAxiom = factory.getOWLClassAssertionAxiom(allValuesFromExpressionVariables, ontModel)
        changes.add(variablesClosureAxiom)

        //val linkFunctionAxiom = factory.getOWLObjectPropertyAssertionAxiom(hasLinkFunction, ontModel, identityLinkFunction)
        //changes.add( linkFunctionAxiom )

        val dataIndependenceAxiom = factory.getOWLDataPropertyAssertionAxiom(isDataIndependent, ontModel, model.hasRepeatedObservations)
        changes.add( dataIndependenceAxiom)

        //val residualDistributionAxiom = factory.getOWLObjectPropertyAssertionAxiom(hasResidualDistribution, ontModel, normalDistribution)
        //changes.add( residualDistributionAxiom )

        //val objectOneOfDistribution = factory.getOWLObjectOneOf(normalDistribution)
        //val allValuesFromExpressionDistribution = factory.getOWLObjectAllValuesFrom(hasResidualDistribution, objectOneOfDistribution)

        //val distributionClosureAxiom = factory.getOWLClassAssertionAxiom(allValuesFromExpressionDistribution, ontModel)
        //changes.add(distributionClosureAxiom)

        manager.applyChanges(manager.addAxioms(ontology, changes))

        //manager.saveOntology(ontology)
//        manager.saveOntology(ontology, new FileOutputStream("/home/mnural/research/analytics/test.owl"))

        retrieveTypes(ontModel, isDirect)

    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Return the ontology entity indicated by the given Variable Type
      *  @param variableType The variable type enum
    */
    def getVariableType(variableType: VariableType): OWLIndividual = {

        sfProvider.getEntity(variableType.qName).asOWLNamedIndividual()

    }
}

object AnalyticsOntology {
    /**
     * The singleton ontology instance
    */
    val ontology = new AnalyticsOntology

    /**
     * Retrieve the OWLOntology instance in the specified way from the factory
    */
    def initOntology : OWLOntology = { AnalyticsOntologyFactory.loadLocal() }


}
// AnalyticsOntology

