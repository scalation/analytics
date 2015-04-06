package apps.analytics

import java.io.FileOutputStream
import java.util

import apps.analytics.VariableTypes.VariableType
import org.semanticweb.HermiT.{Reasoner => HermiTReasoner}
import org.semanticweb.owlapi.model._
import org.semanticweb.owlapi.util.{BidirectionalShortFormProviderAdapter, QNameShortFormProvider}

import scala.collection.JavaConversions._
import scala.collection.mutable.Set

class AnalyticsOntology (ontology: OWLOntology)
{

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** The ontology manager  
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

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** The reasoner used for inference. Loads HermiT Reasoner by default.
      * This might be configurable in the future.
      */
    val hreasoner = (new HermiTReasoner.ReasonerFactory()).createNonBufferingReasoner(ontology)

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
    /** Retrieve the individual identified with the given qualified name.
      *  @param individual OWLNamedIndividual
      *  @param isDirect indicates whether only directly inferenced types
      *                  should be returned or not
      */
    def retrieveTypes(individual: OWLNamedIndividual, isDirect: Boolean = true) : Set[OWLClass] =
    {
        hreasoner.precomputeInferences()
        hreasoner.getTypes(individual, isDirect).getFlattened

    }

    def retrieveTypes(model: Model): Set[OWLClass] ={
        retrieveTypes(model, true)
    }

    def retrieveTypes(model: Model, isDirect: Boolean ) : Set[OWLClass] =
    {
        val changes = new util.HashSet[OWLAxiom]

        val variableClass = factory.getOWLClass(sfProvider.getEntity("analytics:Variable").getIRI)
        //val modelClass = factory.getOWLClass(sfProvider.getEntity("analytics:Model").getIRI)
        //val continuousVariableType = factory.getOWLNamedIndividual( sfProvider.getEntity("analytics:Non_Negative_Variable_Type").getIRI)
        val identityLinkFunction = factory.getOWLNamedIndividual( sfProvider.getEntity("analytics:Identity_Function").getIRI)

        val hasVariable = factory.getOWLObjectProperty( IRI.create(baseIRI + "#hasVariable"))
        val hasResponseVariable = factory.getOWLObjectProperty( IRI.create(baseIRI + "#hasResponseVariable"))
        val hasPredictorVariable = factory.getOWLObjectProperty( IRI.create(baseIRI + "#hasPredictorVariable"))
        val hasVariableType = factory.getOWLObjectProperty( IRI.create(baseIRI + "#hasVariableType"))

        val hasResidualDistribution = factory.getOWLObjectProperty( IRI.create(baseIRI + "#hasResidualDistribution"))
        val hasLinkFunction = factory.getOWLObjectProperty( IRI.create(baseIRI + "#hasLinkFunction"))

        //Create a new individual for this model
        val ontModel = factory.getOWLNamedIndividual( IRI.create( baseIRI + "#" + model.id))

        //val classExpressionAxiom = factory.getOWLClassAssertionAxiom(modelClass, ontModel)
        //val linkFunctionAxiom = factory.getOWLObjectPropertyAssertionAxiom(hasLinkFunction,ontModel,identityLinkFunction)

        //changes.add( classExpressionAxiom )
//        changes.add( linkFunctionAxiom )

        //Create mpg variable (response) and assert axioms
        val variables = Set[OWLIndividual]()
        for (variable <- model.variables){
            val ontVariable = factory.getOWLNamedIndividual( IRI.create( baseIRI + "#" + variable.id))
            val typeAxiom = factory.getOWLClassAssertionAxiom(variableClass,ontVariable)
            changes.add(typeAxiom)

            val variableTypeAxiom = factory.getOWLObjectPropertyAssertionAxiom(hasVariableType, ontVariable, getVariableType(variable.variableType))
            changes.add(variableTypeAxiom)

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

        val objectOneOf = factory.getOWLObjectOneOf(variables)
        val allValuesFromExpression = factory.getOWLObjectAllValuesFrom(hasVariable, objectOneOf)
        val modelRestrictionAxiom = factory.getOWLClassAssertionAxiom(allValuesFromExpression, ontModel)
        changes.add(modelRestrictionAxiom)

        val linkFunctionAxiom = factory.getOWLObjectPropertyAssertionAxiom(hasLinkFunction, ontModel, identityLinkFunction)
        changes.add( linkFunctionAxiom )

        manager.applyChanges(manager.addAxioms(ontology, changes))

        manager.saveOntology(ontology, new FileOutputStream("test.owl"))

        retrieveTypes(ontModel, isDirect)


    }

    def getVariableType(variableType: VariableType): OWLIndividual = {

        sfProvider.getEntity(variableType.qName).asOWLNamedIndividual()

    }
}

// AnalyticsOntology

