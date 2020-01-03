/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.connectors

import play.api.http.Status._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.helpers.ComponentSpecBase
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.SubscriptionRequestSummaryStub
import uk.gov.hmrc.vatsignupfrontend.httpparsers.SubscriptionRequestSummaryHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.{Digital, GeneralPartnership, SubscriptionRequestSummary}


class SubscriptionRequestSummaryConnectorISpec extends ComponentSpecBase {

  lazy val connector: SubscriptionRequestSummaryConnector = app.injector.instanceOf[SubscriptionRequestSummaryConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

 "getSubscriptionRequest" should {
   "return Right SubscriptionRequestSummary for an OK response" in {
     val model =  SubscriptionRequestSummary(
       vatNumber = "vatNumber",
       businessEntity = GeneralPartnership,
       optNino = None,
       optCompanyNumber = None,
       optSautr = Some("sautr"),
       optSignUpEmail = Some("fooEmail"),
       transactionEmail = "barEmail",
       contactPreference = Digital
     )

     SubscriptionRequestSummaryStub.stubGetSubscriptionRequest("vatNumber")(OK, Some(model))
     val res = connector.getSubscriptionRequest("vatNumber")

     await(res).right.get shouldBe model
   }
   "return Left SubscriptionRequestDoesNotExist for an OK response with invalid json" in {
     SubscriptionRequestSummaryStub.stubGetSubscriptionRequestInvalidJson("vatNumber")(OK)
     val res = connector.getSubscriptionRequest("vatNumber")

     await(res).left.get shouldBe SubscriptionRequestUnexpectedError(OK,"JSON does not meet read requirements of SubscriptionRequestSummary")
   }
   "return Left SubscriptionRequestExistsButNotComplete for BAD_REQUEST response" in {
     SubscriptionRequestSummaryStub.stubGetSubscriptionRequest("vatNumber")(BAD_REQUEST, None)
     val res = connector.getSubscriptionRequest("vatNumber")

     await(res).left.get shouldBe SubscriptionRequestExistsButNotComplete
   }
   "return Left SubscriptionRequestDoesNotExist for NOT_FOUND response" in {
     SubscriptionRequestSummaryStub.stubGetSubscriptionRequest("vatNumber")(NOT_FOUND, None)
     val res = connector.getSubscriptionRequest("vatNumber")

     await(res).left.get shouldBe SubscriptionRequestDoesNotExist
   }
   "return Left SubscriptionRequestUnexpectedError for INTERNAL_SERVER_ERROR response" in {
     SubscriptionRequestSummaryStub.stubGetSubscriptionRequest("vatNumber")(INTERNAL_SERVER_ERROR, None)
     val res = connector.getSubscriptionRequest("vatNumber")

     await(res).left.get shouldBe SubscriptionRequestUnexpectedError(INTERNAL_SERVER_ERROR,"Unexpected status from Backend")
   }
 }
}
