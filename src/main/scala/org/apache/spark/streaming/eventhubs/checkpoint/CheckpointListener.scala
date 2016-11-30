/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.streaming.eventhubs.checkpoint

import java.io.IOException

import org.apache.hadoop.fs.Path

import org.apache.spark.internal.Logging
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.scheduler.{StreamingListener, StreamingListenerBatchCompleted}

private[eventhubs] class CheckpointListener extends StreamingListener with Logging {

  override def onBatchCompleted(batchCompleted: StreamingListenerBatchCompleted): Unit = {
    val inputStreamIds = batchCompleted.batchInfo.streamIdToInputInfo.map {
      case (_, batchInfo) =>
        (batchInfo.inputStreamId, batchInfo.numRecords)
    }.filter(_._2 > 0).keys
    inputStreamIds.foreach(streamId => {
      OffsetStoreNew.streamIdToOffstore(streamId).commit(batchCompleted.batchInfo.batchTime)
      logInfo(s"commit checkpoint for batch ${batchCompleted.batchInfo.batchTime}, Stream" +
        s" $streamId")
    })
  }
}
