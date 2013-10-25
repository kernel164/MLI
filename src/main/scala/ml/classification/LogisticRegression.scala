package mli.ml.classification

import mli.interface.impl.MLNumericTable
import mli.ml._
import scala.math
import mli.interface._

class LogisticRegressionModel(
    trainingTbl: MLTable,
    trainingParams: LogisticRegressionParameters,
    trainingTime: Long,
    val weights: MLVector)
  extends NumericModel[LogisticRegressionParameters](trainingTbl, trainingTime, trainingParams) {


  /* Predicts the label of a given data point. */
  def predict(x: MLVector) : MLValue = {
    MLValue(LogisticRegressionAlgorithm.sigmoid(weights dot x))
  }

  /**
   * Provides a user-friendly explanation of this model.
   * For example, plots or console output.
   */
  def explain() : String = {
    "Weights: " + weights.toString
  }
}

case class LogisticRegressionParameters(
    learningRate: Double = 0.2,
    maxIterations: Int = 100,
    minLossDelta: Double = 1e-5,
    optimizer: String = "SGD")
  extends AlgorithmParameters


object LogisticRegressionAlgorithm extends NumericAlgorithm[LogisticRegressionParameters] {

  def defaultParameters() = LogisticRegressionParameters()

  def sigmoid(z: Double): Double = 1.0/(1.0 + math.exp(-1.0*z))

  def train(data: MLNumericTable, params: LogisticRegressionParameters): LogisticRegressionModel = {

    // Initialization
    assert(data.numRows > 0)
    assert(data.numCols > 1)

    val d = data.numCols-1

    def gradient(vec: MLVector, w: MLVector): MLVector = {

      val x = MLVector(vec.slice(1,vec.length))
      val y = vec(0)
      val g = x times (sigmoid(x dot w) - y)
      g
    }

    val startTime = System.currentTimeMillis

    //Run gradient descent on the data.
    val weights = params.optimizer match {
      case "Gradient" => {
        val optParams = opt.GradientDescentParameters(MLVector.zeros(d), gradient, params.minLossDelta)
        opt.GradientDescent(data, optParams)
      }

      case "SGD" => {
        val optParams = opt.StochasticGradientDescentParameters(
          wInit = MLVector.zeros(d),
          grad = gradient,
          learningRate = params.learningRate)
        opt.StochasticGradientDescent(data, optParams)
      }
    }

    val trainTime =  System.currentTimeMillis - startTime

    new LogisticRegressionModel(data.toMLTable, params, trainTime, weights)
  }
}




