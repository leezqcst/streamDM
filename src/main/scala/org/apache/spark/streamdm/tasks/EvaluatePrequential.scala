/*
 * Copyright (C) 2015 Holmes Team at HUAWEI Noah's Ark Lab.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.spark.streamdm.tasks

import com.github.javacliparser.ClassOption
import org.apache.spark.streamdm.core.DenseSingleLabelInstance
import org.apache.spark.streamdm.classifiers.{Learner, SGDLearner}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streamdm.evaluation.Evaluator

class EvaluatePrequential extends Task {

  val learnerOption:ClassOption = new ClassOption("learner", 'l',
    "Learner to use", classOf[Learner], "SGDLearner")

  val evaluatorOption:ClassOption = new ClassOption("evaluator", 'e',
    "Evaluator to use", classOf[Evaluator], "BasicClassificationEvaluator")

  def run(ssc:StreamingContext): Unit = {

    val learner:SGDLearner = this.learnerOption.getValue()
    learner.init()
    val evaluator:Evaluator = this.evaluatorOption.getValue()

    //stream is a localhost socket stream
    val text = ssc.socketTextStream("localhost", 9999)
    //transform stream into stream of instances
    //instances come as tab delimited lines, where the first item is the label,
    //and the rest of the items are the values of the features
    val instances = text.map(
      x => new DenseSingleLabelInstance(x.split("\t").toArray.map(_.toDouble),
        x.split("\t")(0).toDouble))

    //Predict
    val predPairs = learner.predict(instances)

    //Train
    learner.train(instances)

    //Evaluate
    evaluator.addResult(predPairs)

  }
}