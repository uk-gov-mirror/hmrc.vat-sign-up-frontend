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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockVatControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreAdministrativeDivisionHttpParser._
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStoreAdministrativeDivisionService
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec

import scala.concurrent.Future

class DivisionResolverControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockVatControllerComponents
  with MockStoreAdministrativeDivisionService {

  object TestDivisionResolverController extends DivisionResolverController(mockStoreAdministrativeDivisionService)

  lazy val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/division-resolver")

  "calling the resolve method on DivisionResolverController" when {
    "store group information returns StoreDivisionInformationSuccess" should {
      "goto agree capture email" in {
        mockAuthAdminRole()
        mockStoreAdministrativeDivision(testVatNumber)(Future.successful(Right(StoreAdministrativeDivisionSuccess)))

        val res = TestDivisionResolverController.resolve(testGetRequest.withSession(
          SessionKeys.vatNumberKey -> testVatNumber
        ))

        status(res) shouldBe SEE_OTHER
        redirectLocation(res) shouldBe Some(routes.DirectDebitResolverController.show().url)
      }
    }
    "store group information returns StoreDivisionInformationFailureResponse" should {
      "throw internal server exception" in {
        mockAuthAdminRole()
        mockStoreAdministrativeDivision(testVatNumber)(Future.successful(Left(StoreAdministrativeDivisionFailureResponse(INTERNAL_SERVER_ERROR))))

        intercept[InternalServerException] {
          TestDivisionResolverController.resolve(testGetRequest.withSession(
            SessionKeys.vatNumberKey -> testVatNumber
          ))
        }
      }
    }
    "vat number is not in session" should {
      "goto resolve vat number" in {
        mockAuthAdminRole()
        mockStoreAdministrativeDivision(testVatNumber)(Future.successful(Right(StoreAdministrativeDivisionSuccess)))

        val res = TestDivisionResolverController.resolve(testGetRequest)

        status(res) shouldBe SEE_OTHER
        redirectLocation(res) shouldBe Some(routes.ResolveVatNumberController.resolve().url)
      }
    }
  }
}

