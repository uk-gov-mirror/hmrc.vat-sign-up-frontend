/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.services

import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.connectors.SubscriptionRequestSummaryConnector
import uk.gov.hmrc.vatsignupfrontend.httpparsers.SubscriptionRequestSummaryHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.{Digital, Division, SubscriptionRequestSummary}

import scala.concurrent.Future

class SubscriptionRequestSummaryServiceSpec extends UnitSpec with MockitoSugar {

  val mockConnector = mock[SubscriptionRequestSummaryConnector]
  implicit val hc = HeaderCarrier()

  class Setup {
    val service = new SubscriptionRequestSummaryService(mockConnector)
  }
  val vatNumber = "vatNumber"

  "getSubscriptionSummaryRequest" should {
    "return a Left of SubscriptionRequestExistsButNotComplete if connector returns SubscriptionRequestExistsButNotComplete" in new Setup{
      when(mockConnector.getSubscriptionRequest(ArgumentMatchers.eq(vatNumber))(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Left(SubscriptionRequestExistsButNotComplete)))

      await(service.getSubscriptionSummaryRequest(vatNumber)) shouldBe Left(SubscriptionRequestExistsButNotComplete)
    }
    "return a Left of SubscriptionRequestDoesNotExist if connector returns SubscriptionRequestDoesNotExist" in new Setup {
      when(mockConnector.getSubscriptionRequest(ArgumentMatchers.eq(vatNumber))(ArgumentMatchers.any()))
        .thenReturn(Future.successful(Left(SubscriptionRequestDoesNotExist)))

      await(service.getSubscriptionSummaryRequest(vatNumber)) shouldBe Left(SubscriptionRequestDoesNotExist)
    }
    "return a Left of SubscriptionRequestUnexpectedError if connector returns SubscriptionRequestUnexpectedError" in new Setup {
      when(mockConnector.getSubscriptionRequest(ArgumentMatchers.eq(vatNumber))(ArgumentMatchers.any()))
        .thenReturn(Future.successful(Left(SubscriptionRequestUnexpectedError(Status.INTERNAL_SERVER_ERROR, "foo"))))

      await(service.getSubscriptionSummaryRequest(vatNumber)) shouldBe Left(SubscriptionRequestUnexpectedError(Status.INTERNAL_SERVER_ERROR, "foo"))
    }
    "return a right of SubscriptionRequestSummary if connector returns SubscriptionRequestSummary" in new Setup {
      val expectedModel = SubscriptionRequestSummary(
        vatNumber,
        Division,
        None,
        "emailFoo",
        Digital
      )
      when(mockConnector.getSubscriptionRequest(ArgumentMatchers.eq(vatNumber))(ArgumentMatchers.any()))
        .thenReturn(Future.successful(Right(expectedModel)))

      await(service.getSubscriptionSummaryRequest(vatNumber)) shouldBe Right(expectedModel)
    }
  }

}
