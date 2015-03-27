package apps.analytics

import org.semanticweb.HermiT.{Reasoner => HermiTReasoner}
import org.semanticweb.owlapi.model.{OWLEntity, OWLNamedIndividual, OWLOntology}
import org.semanticweb.owlapi.util.{BidirectionalShortFormProviderAdapter, QNameShortFormProvider}

class AnalyticsOntology (ontology: OWLOntology)
{

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** The ontology manager  
     */
    val manager = ontology.getOWLOntologyManager

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** The short form provider to avoid using the full IRI of an entity 
      * during search. Currently, it is using qname short form provider which 
      * allows forms such as owl:Thing, analytics:Model, etc.
      */
    val sfProvıder = new BidirectionalShortFormProviderAdapter(manager, ontology.getImportsClosure(), new QNameShortFormProvider())

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** The reasoner used for inference. Loads HermiT Reasoner by default.
      * This might be configurable in the future.
      */
    val hreasoner = (new HermiTReasoner.ReasonerFactory()).createReasoner (ontology)

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Retrieve short form of an entity as a String.  
      *  @param entity the entity for which the short form is required.
      */
    def getShortForm(entity: OWLEntity): String = sfProvıder.getShortForm(entity)

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Retrieve the individual identified with the given qualified name.
      *  @param qName  The qualified name of the individual. eg., analytics:GenericModel.
      */
    def retrieveIndividual (qName : String) : OWLNamedIndividual =
    {
        ontology.getEntitiesInSignature(sfProvıder.getEntity(qName).getIRI).iterator().next().asOWLNamedIndividual()
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Retrieve the individual identified with the given qualified name.
      *  @param individual OWLNamedIndividual
      *  @param isDirect indicates whether only directly inferenced types
      *                  should be returned or not
      */
    def retrieveTypes(individual: OWLNamedIndividual, isDirect: Boolean = true) =
    {
        hreasoner.precomputeInferences()
        hreasoner.getTypes(individual, isDirect).getFlattened
    }

}

// AnalyticsOntology

