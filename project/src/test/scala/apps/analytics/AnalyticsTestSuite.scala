package apps.analytics

import org.scalatest._
import org.semanticweb.HermiT.{Reasoner => HermiTReasoner}
import uk.ac.manchester.cs.jfact.JFactFactory

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
            val (manager, ontology) = AnalyticsOntology.loadLocal ()
            val hreasoner = (new HermiTReasoner.ReasonerFactory()).createReasoner (ontology);
            val jreasoner = (new JFactFactory()).createReasoner (ontology)
            val factory   = manager.getOWLDataFactory ()
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

} // AnalyticsTestSuite
