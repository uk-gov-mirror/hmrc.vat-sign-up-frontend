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

package uk.gov.hmrc.vatsubscriptionfrontend.services

import org.mockito.ArgumentMatchers
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Mockito._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import play.api.http.Status._
import uk.gov.hmrc.vatsubscriptionfrontend.connectors.StoreSubscriptionDetailsConnector

import scala.concurrent.ExecutionContextExecutor

class StoreSubscriptionDetailsServiceSpec extends UnitSpec with MockitoSugar{

  implicit val hc = HeaderCarrier()
  implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.Implicits.global

  object TestStoreSubscriptionDetailsService extends StoreSubscriptionDetailsService(
    mock[StoreSubscriptionDetailsConnector]
  )

  "The StoreSubscriptionDetailsService" should {

    import TestStoreSubscriptionDetailsService._

    "return a successful response" when {

      "it has successfully stored the vat number" in {
        val testVatNumber = "123456789"

        when(storeSubscriptionDetailsConnector.storeVatNumber(ArgumentMatchers.eq(testVatNumber))(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(HttpResponse(NO_CONTENT))
        val result = await(TestStoreSubscriptionDetailsService.storeVatNumber(testVatNumber))
        result shouldBe Right(StoreSubscriptionDetailsSuccess)
      }

    }

    "return a failure response" when {

      "it has failed to store the vat number" in {
        val testVatNumber = "987654321"

        when(storeSubscriptionDetailsConnector.storeVatNumber(ArgumentMatchers.eq(testVatNumber))(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(HttpResponse(INTERNAL_SERVER_ERROR))
        val result = await(TestStoreSubscriptionDetailsService.storeVatNumber(testVatNumber))
        result shouldBe Left(StoreSubscriptionDetailsFailure)
      }

    }
  }

}
