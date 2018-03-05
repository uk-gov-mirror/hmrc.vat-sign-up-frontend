/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import play.api.http.Status._

object TestConstants {
  /*
  * this nino is a constant, if you need a fresh one use TestModels.newNino
  */
  lazy val testNino = utils.TestConstants.testNino
  lazy val testUtr = utils.TestConstants.testUtr
  lazy val testMTDID = utils.TestConstants.testMTDID
  //Not a valid MTDID, for test purposes only
  lazy val startDate = utils.TestConstants.startDate
  lazy val endDate = utils.TestConstants.endDate

  lazy val knownFactsRequest = KnownFactsRequest(
    List(
      TypeValuePair(mtdItsaEnrolmentIdentifierKey, testMTDID),
      TypeValuePair(agentServiceIdentifierKey, testNino)
    )
  )

  val testErrorMessage = "This is an error"
  val testException = core.utils.TestConstants.testException


  val testSubscriptionSuccess = Right(SubscriptionSuccess(testMTDID))

  val testSubscriptionFailure = Left(SubscriptionFailureResponse(INTERNAL_SERVER_ERROR))

  val testKnownFactsSuccess = Right(KnownFactsSuccess)

  val testKnownFactsFailure = Left(KnownFactsFailure(testErrorMessage))

  lazy val testLockoutResponse = core.utils.TestConstants.testLockoutResponse

}
