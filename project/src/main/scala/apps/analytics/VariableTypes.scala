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
    case object Continuous extends VariableType("Continuous_Variable_Type", "Continuous Variable Type")
    case object Non_Negative_Continuous extends VariableType("Non_Negative_Continuous_Variable_Type", "Non Negative Continuous Variable Type")

    //Discrete Variable Types
    case object Discrete extends VariableType("Discrete_Variable_Type", "Discrete Variable Type")

    case object Categorical extends VariableType("Categorical_Variable_Type", "Categorical Variable Type")
    case object Binary extends VariableType("Binary_Variable_Type", "Binary Variable Type")
    case object Ordinal extends VariableType("Ordinal_Variable_Type", "Ordinal Variable Type")
    case object Integer extends VariableType("Integer_Variable_Type", "Integer Variable Type")
    case object Non_Negative_Integer extends VariableType("Non_Negative_Integer_Variable_Type", "Non Negative Integer Variable Type")

}
