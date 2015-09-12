package apps.analytics.dashboard.model


/**
 * Created by mnural on 9/10/15.
 */
object ModelTypes {

  /** The sealed abstract class ModelType to provide enumeration of Model Types
    * @param ontologyID The ontology ID of this model type. This needs to correspond
    *                   to the ID in the Analytics ontology
    * @param label    The descriptive label for this model.
    */
  sealed abstract class ModelType(
                                      val ontologyID    : String,
                                      val label         : String
                                    ){

    // The qualified name in Analytics ontology for the model class
    val qName = "analytics:" + ontologyID

    // Return label by default
    override def toString = label
  }

  //Root Class
  case object Model extends ModelType("Model", "Model")

  //Time Dependent Models
  case object TimeDependentModel extends ModelType("TimeDependentModel", "Time Dependent Model")
  case object ARIMA extends ModelType("ARIMA", "ARIMA")
  case object ARMA extends ModelType("ARMA", "ARMA")
  case object AR extends ModelType("AR", "AR")
  case object MA extends ModelType("MA", "MA")

  //Time Independent Models
  case object TimeIndependentModel extends ModelType("TimeIndependentModel", "Time Independent Model")
  case object GEE extends ModelType("GEE", "General Estimating Equations")
  case object GLMM extends ModelType("GLMM", "Generalized Linear Mixed Model")

  case object GZLM extends ModelType("GZLM", "Generalized Linear Model")
  case object ExponentialRegression extends ModelType("Exponential_Regression", "Exponential Regression")
  case object GammaRegression extends ModelType("Gamma_Regression", "Gamma Regression")

  case object GLM extends ModelType("GLM", "General Linear Model")

  case object ANCOVA extends ModelType("ANCOVA", "ANCOVA")
  case object ANOVA extends ModelType("ANOVA", "ANOVA")

  case object MultipleLinearRegression extends ModelType("Multiple_Linear_Regression", "Multiple Linear Regression")
  case object SimpleLinearRegression extends ModelType("Simple_Linear_Regression", "Simple Linear Regression")

  case object PolynomialRegression extends ModelType("Polynomial_Regression", "Polynomial Regression")
  case object ResponseSurfaceAnalysis extends ModelType("Response_Surface_Analysis", "Response Surface Analysis")
  case object TransformedMultipleLinearRegression extends ModelType("Transformed_Multiple_Linear_Regression", "Transformed Multiple Linear Regression")
  case object TrigonometricRegression extends ModelType("Trigonometric_Regression", "Trigonometric Regression")

  case object LogisticRegression extends ModelType("Logistic_Regression", "Logistic Regression")
  case object MultinomialLogisticRegression extends ModelType("Multinomial_Logistic_Regression", "Multinomial Logistic Regression")
  case object NegativeBinomialRegression extends ModelType("Negative_Binomial_Regression", "Negative Binomial Regression")
  case object OrdinalLogisticRegression extends ModelType("Ordinal_Logistic_Regression", "Ordinal Logistic Regression")
  case object PoissonRegression extends ModelType("Poisson_Regression", "Poisson Regression")

  case object NaiveBayes extends ModelType("Naive_Bayes", "Naive Bayes")
  case object Perceptron extends ModelType("Perceptron", "Perceptron")

  //Enumeration of all model types.
  val values = List(Model, TimeDependentModel, ARIMA, ARMA, AR, MA, TimeIndependentModel, GEE, GLMM, GZLM,
    ExponentialRegression, GammaRegression, GLM, ANCOVA, ANOVA, MultipleLinearRegression, SimpleLinearRegression,
    PolynomialRegression, ResponseSurfaceAnalysis, TransformedMultipleLinearRegression, TrigonometricRegression,
    LogisticRegression, MultinomialLogisticRegression, NegativeBinomialRegression, OrdinalLogisticRegression,
    PoissonRegression, NaiveBayes, Perceptron
  )
}
