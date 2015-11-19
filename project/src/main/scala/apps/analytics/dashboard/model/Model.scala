package apps.analytics.dashboard.model

import java.net.URL

import apps.analytics.dashboard.AnalyticsOntology
import org.semanticweb.owl.explanation.api._
import org.semanticweb.owlapi.model._

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scalation.relalgebra.Relation

/**
 * Model provides a conceptual representation of a dataset
 * @author Mustafa Nural
 * Created by mnural on 3/29/15.
 */
class Model (val file : URL = null, val delimiter : String = ",", var hasRepeatedObservations : Boolean = false, var mergeDelims : Boolean = false){
  var relation : Relation = null


  /**
   * Alternative constructor in case no dataset is specified
   * @param hasRepeatedObservations
   */
  def this(hasRepeatedObservations : Boolean) = {
    this(null, ",", hasRepeatedObservations)
  }

  val id = "Model" + System.currentTimeMillis() //append current timestamp to create a unique ID for ontology

  //Conceptual Properties

  //reference to the AnalyticsOntology object to reasoning support
  val ontology = AnalyticsOntology.ontology

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
    ontology.retrieveTypes(this)
  }


  //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
  /** Gets user friendly label for the given OWLClass
    * @param ontologyModel The model class
    * @return label from the ontology if one exists, the OWLClass name otherwise
    */

  def getLabel(ontologyModel: OWLClass): String = {
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

  /**
   * Retrieves all annotations of type "rdf:comment" for the given axiom
   * @param owlAxiom The axiom to get the annotations for
   * @return A concatenated String containing annotations for this axiom
   */
  def getAnnotation(owlAxiom: OWLAxiom): String = {
    val annot = new StringBuilder()
    val comments = owlAxiom.getAnnotations.filter(annotation => annotation.getProperty.isComment)
    comments.foreach(c => annot.append(c.getValue.asInstanceOf[OWLLiteral].getLiteral))
    annot.toString()
  }

  /** Retrieves possible explanations for the given suggested model.
    * @param suggestedModel The suggested model class
    * @return The set of possible explanations for this suggestion
    */

  def getExplanation(suggestedModel: OWLClass) : mutable.Set[String] = {
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
    val explanations = expressions.map(f => getAnnotation(f))
    println(expressions)

    explanations.filterNot(expl => expl.isEmpty)
  }

  override def toString = {
    var summary = new StringBuilder()

    variables.filter(_.isResponse).foreach(v => summary ++= v.label + "(" + v.variableType + ")" + " ~ ")
    variables.filterNot(v => v.ignore || v.isResponse).foreach(v=> summary ++= v.label + "(" + v.variableType + ")" +" + ")

    summary.delete(summary.length - 2, summary.length)
    summary ++= "\n\n"

    summary ++=  "Has repeated observations? : " + {if(hasRepeatedObservations) "Yes" else "No"} + "\n"
//    summary ++= "\n"
//    variables.filterNot(_.ignore).foreach(summary ++= "\t" + _.toString + "\n")
    summary.toString
  }
}
