package apps.analytics

import apps.analytics.dashboard.AnalyticsOntologyFactory
import apps.analytics.dashboard.model.ModelTypes
import apps.analytics.model.VariableTypes
import org.scalatest._
import org.semanticweb.HermiT.{Reasoner => HermiTReasoner}
import org.semanticweb.owlapi.model.IRI
import uk.ac.manchester.cs.jfact.JFactFactory

import scala.collection.JavaConverters._
import scala.language.reflectiveCalls

/**
 *
 */
class AnalyticsTestSuite extends FunSuite with GivenWhenThen
{

  /** This returns the shared fixture containing the things
    * the tests need to do their work.
    */
  def fixture = {
    new {
      val ontology = AnalyticsOntologyFactory.loadLocal ()
      val hreasoner = (new HermiTReasoner.ReasonerFactory()).createReasoner (ontology);
      val jreasoner = (new JFactFactory()).createReasoner (ontology)
      val factory   = ontology.getOWLOntologyManager.getOWLDataFactory ()
    } // new
  } // fixture

  test ("The ontology should be consistent with all supported reasoners")
  {
    // test with HermiT
    Given ("the unaltered ontology")
    When ("using HermiT")
    Then ("the ontology should be consistent")
    assert (fixture.hreasoner.isConsistent ())

    // test with JFact
    Given ("the unaltered ontology")
    When ("using JFact")
    Then ("the ontology should be consistent")
    assert (fixture.jreasoner.isConsistent ())
  } // test

  test("The model class in the ontology should have a corresponding enum in ModelTypes ")
  {
    Given("The Analytics Ontology")
    When("iterating over all subclasses of 'Model'")
    Then("A corresponding case object should exist in the ModelTypes")
    val factory = fixture.factory
    val baseIRI = fixture.ontology.getOntologyID.getOntologyIRI
    val modelClass = factory.getOWLClass(IRI.create(baseIRI + "#Model"))
    val modelTypes = ModelTypes.values.map(_.ontologyID)

    val subClasses = fixture.hreasoner.getSubClasses(modelClass, false).getFlattened.asScala.filterNot(_.isBottomEntity)

    val missingClasses = subClasses.filterNot(
      subClass => modelTypes.contains(subClass.asOWLClass().getIRI.getRemainder.get())
    )

//    println("Missing Classes" + missingClasses)
    assert(missingClasses.isEmpty)

  }

  test("The each variable type class in the ontology should have a corresponding enum in VariableTypes")
  {
    Given("The Analytics Ontology")
    When("iterating over all instances of 'VariableType'")
    Then("A corresponding case object should exist in the VariableTypes")
    val factory = fixture.factory
    val baseIRI = fixture.ontology.getOntologyID.getOntologyIRI
    val variableTypeClass = factory.getOWLClass(IRI.create(baseIRI + "#Variable_Type"))
    val variableTypes = VariableTypes.values.map(_.ontologyID)

    val individuals = fixture.hreasoner.getInstances(variableTypeClass, false).getFlattened

    val missingIndividuals = individuals.asScala.filterNot(
      individual => variableTypes.contains(individual.getIRI.getRemainder.get())
    )

//    println("Missing Individuals" + missingIndividuals)
    assert(missingIndividuals.isEmpty)

  }

} // AnalyticsTestSuite
