package apps.analytics

/**
 * Created by mnural on 4/3/15.
 */
object VariableTypes{

    /** The sealed abstract class VariableType to provide enumeration of Variable Types
      * @param ontologyID The ontology ID of this variable type. This needs to correspond
      *                   to the ID in the Analytics ontology
      * @param label    The descriptive label for this variable type.
      */
    sealed abstract class VariableType(
        val ontologyID    : String,
        val label         : String
    ){

        // The qualified name in Analytics ontology for the variable type
        val qName = "analytics:" + ontologyID

        // Return label by default
        override def toString = label
    }

    //Continuous Variable Types
    case object Continuous extends VariableType("Continuous", "Continuous Variable Type")
    case object Non_Negative_Continuous extends VariableType("Non_Negative_Continuous_Variable_Type", "Non Negative Continuous Variable Type")

    //Discrete Variable Types
    case object Discrete extends VariableType("Discrete", "Discrete Variable Type")

    case object Categorical extends VariableType("Categorical", "Categorical Variable Type")
    case object Binary extends VariableType("Binary", "Binary Variable Type")
    case object Ordinal extends VariableType("Ordinal", "Ordinal Variable Type")
    case object Integer extends VariableType("Integer", "Integer Variable Type")
    case object Non_Negative_Integer extends VariableType("Non_Negative_Integer", "Non Negative Integer Variable Type")

}
