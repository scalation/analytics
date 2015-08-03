package apps.analytics.dashboard.model

import apps.analytics.dashboard.AnalyticsOntology
import org.semanticweb.owl.explanation.api._
import org.semanticweb.owlapi.model._

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Created by mnural on 3/29/15.
 */
class Model (val hasRepeatedObservations : Boolean = false){

    val id = "Model" + System.currentTimeMillis() //append current timestamp to create a unique ID for ontology

    //Conceptual Properties

    //List of model types inferred by the reasoner
    private var modelTypes : mutable.Set[OWLClass] = null

    //Variables
    val variables = ArrayBuffer[Variable]()

    //The link function if this model is a GZLM model
    var linkFunction = null

    var residualDistribution = null

    //Runtime/Algorithmic Properties.
    //Add type once Algorithm class is created.
    val algorithms = ArrayBuffer()

    //The data properties
    //val dataset : MatrixD = null
    //val responseColumn = null

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** This method returns suitable model types from the ontology for the current
      * state of this conceptual model
      * @return suggested models from the analytics ontology for this object
      */
    def getModelTypes = {
        val ontology = AnalyticsOntology.ontology
        modelTypes = ontology.retrieveTypes(this)
        modelTypes
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Gets user friendly label for the given OWLClass
      * @param ontologyModel The model class
      * @return label from the ontology if one exists, the OWLClass name otherwise
      */

    def getLabel(ontologyModel: OWLClass): String = {
        val ontology = AnalyticsOntology.ontology
        for (annotation : OWLAnnotation <- ontologyModel.getAnnotations(ontology.ontology, ontology.factory.getRDFSLabel)) {
            if (annotation.getValue .isInstanceOf[OWLLiteral]) {
                val value: OWLLiteral = annotation.getValue().asInstanceOf[OWLLiteral];
                if (value.getLiteral() != null){
                    return value.getLiteral
                }
            }
        }
        return ontologyModel.getIRI.getRemainder.toString
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

  def getAnnotation(f: OWLAxiom): String = {
    //TODO Look why this returns empty all the time.
    val annot = new StringBuilder()
    val comments = f.getAnnotations.filter(annotation => annotation.getProperty.isComment)
    comments.map(c => annot + c.getValue.toString)
    annot.toString()
  }

  /** Retrieves possible explanations for the given suggested model.
      * @param suggestedModel The suggested model class
      * @return The set of possible explanations for this suggestion
      */

    def getExplanation(suggestedModel: OWLClass) : mutable.Set[String] = {
      val ontology = AnalyticsOntology.ontology
      val modelIndividual = ontology.sfProvider.getEntity("analytics:" + this.id).asOWLNamedIndividual()
      val entailment : OWLAxiom = ontology.factory.getOWLClassAssertionAxiom(suggestedModel, modelIndividual)

      // Create the explanation generator factory which uses reasoners provided by the specified
      // reasoner factory
      val genFac: ExplanationGeneratorFactory[OWLAxiom] = ExplanationManager.createExplanationGeneratorFactory(ontology.reasonerFactory);

      // Now create the actual explanation generator for our ontology
      val gen: ExplanationGenerator[OWLAxiom] = genFac.createExplanationGenerator(ontology.ontology);

      // Get our explanations.  Ask for a maximum of 1
      val expl = gen.getExplanations(entailment, 1).asScala.toArray;
      val expressions = expl(0).getAxioms.filter( axiom => axiom.isInstanceOf[OWLEquivalentClassesAxiom])
      val explanations = expressions.map(f => getAnnotation(f)).filterNot(expl => expl.isEmpty)
      println(expressions)

      return explanations
    }

}
