package apps.analytics

import java.io.File
import java.net.URI

import org.semanticweb.HermiT.{ Reasoner => HermiTReasoner }
import org.semanticweb.owlapi.model.{ IRI, OWLOntology, OWLOntologyManager }
import org.semanticweb.owlapi.apibinding.OWLManager

object AnalyticsOntology
{

    /** The return type of various loading functions. */
    type MO = Tuple2[OWLOntologyManager, OWLOntology]

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** The default IRI for the ScalaTion Analytics Ontology 
     */
    val remoteIRI = IRI.create("https://raw.githubusercontent.com/scalation/analytics/master/analytics.owl")

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** A local IRI for the ScalaTion Analytics Ontology based on a `File` path.
     */
    def localIRI (file: File) = IRI.create(file.toURI())

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Load an ontology from a given IRI 
     *  @param iri  the IRI of the ontology
     */
    private def load (iri: IRI): MO =
    {
        val manager  = OWLManager.createOWLOntologyManager()
        val ontology = manager.loadOntologyFromOntologyDocument(iri)
        (manager, ontology)
    } // load

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Load the ScalaTion Analytics Ontology locally from the default file
     *  path. 
     */
    def loadLocal (): MO =
    {
        load(AnalyticsOntology.localIRI(new File("../analytics.owl")))
    } // loadLocal

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Load the ScalaTion Analytics Ontology locally from the specified file
     *  path. 
     *   @parm file  the file containing the ontology
     */
    def loadLocal (file: File): MO =
    {
        load(AnalyticsOntology.localIRI(file))
    } // loadLocal

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Load the ScalaTion Analytics Ontology locally from the default remote
     *  location.
     */
    def loadRemote (): MO =
    {
        load(AnalyticsOntology.remoteIRI)
    } // loadRemote

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Load the ScalaTion Analytics Ontology locally from the specified remote
     *  location.
     *   @param uri  the URI of the remote ontology
     */
    def loadRemote (uri: URI): MO =
    {
        load(IRI.create(uri))
    } // loadRemote

} // AnalyticsOntology

