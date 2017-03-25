package apps.analytics.model

import scala.collection.immutable.HashSet
import scalation.linalgebra.{Vec, VectorD, VectorI, VectorS}

/**
 * Created by mnural on 4/3/15.
 */
object VariableTypes{

  /** The sealed abstract class VariableType to provide enumeration of Variable Types
    *
    * @param ontologyID The ontology ID of this variable type. This needs to correspond
    *                   to the ID in the Analytics ontology
    * @param label    The descriptive label for this variable type.
    */
  sealed abstract class VariableType(
                                      val ontologyID    : String,
                                      val label         : String,
                                      val isNumeric     : Boolean = true
                                      ){

    // The qualified name in Analytics ontology for the variable type
    val qName = "analytics:" + ontologyID

    // Return label by default
    override def toString = label
  }


  //Continuous Variable Types
  case object Continuous extends VariableType("Continuous_Variable_Type", "Continuous", true)
  case object Non_Negative_Continuous extends VariableType("Non_Negative_Continuous_Variable_Type", "Non Negative Continuous", true)

  //Discrete Variable Types
  case object Discrete extends VariableType("Discrete_Variable_Type", "Discrete")

  case object Categorical extends VariableType("Categorical_Variable_Type", "Categorical")
  case object Binary extends VariableType("Binary_Variable_Type", "Binary")
  case object Ordinal extends VariableType("Ordinal_Variable_Type", "Ordinal")
  case object Integer extends VariableType("Integer_Variable_Type", "Integer")
  case object Non_Negative_Integer extends VariableType("Non_Negative_Integer_Variable_Type", "Non Negative Integer")

  //Non-numeric
  case object String extends VariableType("String_Variable_Type", "String", false)
  case object Constant extends VariableType("Constant_Variable_Type", "String" , false)

  val values = List(Continuous, Non_Negative_Continuous, Discrete, Categorical, Binary, Ordinal, Integer, Non_Negative_Integer)

  val numeric = List(Continuous, Non_Negative_Continuous, Integer, Non_Negative_Integer)
  val nominal = List(Categorical, Ordinal)


  /**
    * Given a value set, this method tries to infer the domain.
    *
    * @param column The data vector
    * @param typ The column type
    * @return Inferred VariableType for the data vector.
    */
  def inferVariableType(column : Vec, typ : Char): VariableType = {

    val valueSet = {
      typ match {
        case 'I' => HashSet(column.asInstanceOf[VectorI].toSeq:_* )
        case 'D' => HashSet(column.asInstanceOf[VectorD].toSeq:_*)
        case _ => HashSet(column.asInstanceOf[VectorS].toSeq:_*)
      }
    }
    valueSet.size match {
      case 1 =>
        //TODO HANDLE THIS CASE
        //Create "Constant" Variable Type?
        println("CONSTANT")
        VariableTypes.Constant
      case 2 =>
        VariableTypes.Binary
      case it if 3 until 10 contains it =>
        VariableTypes.Categorical
      case _ => {
        typ match{
          case 'I' =>
            if (valueSet.asInstanceOf[Set[Int]].exists(_ < 0)){
              VariableTypes.Integer
            } else{
              VariableTypes.Non_Negative_Integer
            }
          case 'D' =>
            if (valueSet.asInstanceOf[Set[Double]].exists(_ < 0)){
              VariableTypes.Continuous
            } else{
              VariableTypes.Non_Negative_Continuous
            }
          case _ =>
            VariableTypes.String
        }
        //        try{
        //          val doubleList = valueSet.map(_.toDouble)
        //          // check for ordinal
        //          val sortedDouble = (collection.immutable.SortedSet[Double]() ++ doubleList).toBuffer
        //          if (sortedDouble.size >= 2){
        //            for (i <- 1 until sortedDouble.size){
        //              sortedDouble(i-1) = sortedDouble(i) - sortedDouble(i-1)
        //            }
        //          } // if
        //          sortedDouble.remove(sortedDouble.size-1)
        //
        //          if (sortedDouble.toSet.size == 1){
        //            return VariableTypes.Ordinal
      }
    }
  }
}
