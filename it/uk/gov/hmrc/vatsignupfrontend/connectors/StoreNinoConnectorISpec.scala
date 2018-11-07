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

package uk.gov.hmrc.vatsignupfrontend.connectors

import java.time.LocalDate
import java.util.UUID

import play.api.http.Status._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.helpers.ComponentSpecBase
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreNinoStub
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreNinoHttpParser.{NoMatchFoundFailure, NoVATNumberFailure, StoreNinoFailureResponse, StoreNinoSuccess}
import uk.gov.hmrc.vatsignupfrontend.models.{DateModel, UserDetailsModel, UserEntered}

import scala.concurrent.ExecutionContext.Implicits.global

class StoreNinoConnectorISpec extends ComponentSpecBase {

  lazy val connector: StoreNinoConnector = app.injector.instanceOf[StoreNinoConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  val testUserDetails = UserDetailsModel(
    firstName = UUID.randomUUID().toString,
    lastName = UUID.randomUUID().toString,
    dateOfBirth = DateModel.dateConvert(LocalDate.now()),
    nino = testNino
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  "storeNino" when {
    "Backend returns a NO_CONTENT response" should {
      "return StoreNinoSuccess" in {
        StoreNinoStub.stubStoreNino(testVatNumber, testUserDetails, UserEntered)(NO_CONTENT)

        val res = connector.storeNino(testVatNumber, testUserDetails, UserEntered)

        await(res) shouldBe Right(StoreNinoSuccess)
      }
    }
  }

  "Backend returns a FORBIDDEN response" should {
    "return the nino returned" in {
      StoreNinoStub.stubStoreNino(testVatNumber, testUserDetails, UserEntered)(FORBIDDEN)

      val res = connector.storeNino(testVatNumber, testUserDetails, UserEntered)

      await(res) shouldBe Left(NoMatchFoundFailure)
    }
  }

  "Backend returns a NOT_FOUND response" should {
    "return the nino returned" in {
      StoreNinoStub.stubStoreNino(testVatNumber, testUserDetails, UserEntered)(NOT_FOUND)

      val res = connector.storeNino(testVatNumber, testUserDetails, UserEntered)

      await(res) shouldBe Left(NoVATNumberFailure)
    }
  }

  "Backend returns a BAD_REQUEST response" should {
    "return a UserMatchFailureResponseModel" in {
      StoreNinoStub.stubStoreNino(testVatNumber, testUserDetails, UserEntered)(BAD_REQUEST)

      val res = connector.storeNino(testVatNumber, testUserDetails, UserEntered)

      await(res) shouldBe Left(StoreNinoFailureResponse(BAD_REQUEST))
    }
  }

}