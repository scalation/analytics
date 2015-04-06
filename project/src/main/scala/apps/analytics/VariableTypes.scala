package apps.analytics

/**
 * Created by mnural on 4/3/15.
 */
object VariableTypes{

    sealed abstract class VariableType(
        val ontologyID    : String,
        val label         : String
    ){
        val qName = "analytics:" + ontologyID
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
