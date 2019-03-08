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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreOverseasInformationHttpParser._
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStoreOverseasInformationService

import scala.concurrent.Future

class OverseasResolverControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents
  with MockStoreOverseasInformationService {

  object TestOverseasResolverController extends OverseasResolverController(
    mockControllerComponents,
    mockStoreOverseasInformationService
  )

  lazy val testGetRequest = FakeRequest("GET", "/overseas-resolver")

  "calling the resolve method on OverseasResolverController" when {
    "store overseas information returns StoreOverseasInformationSuccess" should {
      "goto agree capture email" in {
        mockAuthAdminRole()
        mockStoreOverseasInformation(testVatNumber)(Future.successful(Right(StoreOverseasInformationSuccess)))

        val res = await(TestOverseasResolverController.resolve(testGetRequest.withSession(
          SessionKeys.vatNumberKey -> testVatNumber
        )))

        status(res) shouldBe SEE_OTHER
        redirectLocation(res) shouldBe Some(routes.DirectDebitResolverController.show().url)
      }
    }
    "store overseas information returns StoreOverseasInformationFailureResponse" should {
      "throw internal server exception" in {
        mockAuthAdminRole()
        mockStoreOverseasInformation(testVatNumber)(Future.successful(Left(StoreOverseasInformationFailureResponse(INTERNAL_SERVER_ERROR))))

        intercept[InternalServerException] {
          await(TestOverseasResolverController.resolve(testGetRequest.withSession(
            SessionKeys.vatNumberKey -> testVatNumber
          )))
        }
      }
    }
    "vat number is not in session" should {
      "goto resolve vat number" in {
        mockAuthAdminRole()
        mockStoreOverseasInformation(testVatNumber)(Future.successful(Right(StoreOverseasInformationSuccess)))

        val res = await(TestOverseasResolverController.resolve(testGetRequest))

        status(res) shouldBe SEE_OTHER
        redirectLocation(res) shouldBe Some(routes.ResolveVatNumberController.resolve().url)
      }
    }
  }

}
