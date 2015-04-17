package apps.analytics

import scala.collection.mutable.ArrayBuffer

/**
 * Created by mnural on 3/29/15.
 */
class Model (val hasRepeatedObservations : Boolean = false){

    val id = "Model" + System.currentTimeMillis() //append current timestamp to create a unique ID for ontology

    //Conceptual Properties

    //List of model types inferred by the reasoner
    private var modelTypes = null

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
      */
    def getModelTypes = {
        val ontology = new AnalyticsOntology(AnalyticsOntologyFactory.loadLocal())
        ontology.retrieveTypes(this)

    }

}
