/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.soletrader

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockVatControllerComponents
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.{CaptureVatNumberController, DirectDebitResolverController}
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStoreNinoService
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec

class SoleTraderResolverControllerSpec extends UnitSpec
  with GuiceOneAppPerSuite
  with MockVatControllerComponents
  with MockStoreNinoService {

  object TestSoleTraderResolverController extends SoleTraderResolverController(mockStoreNinoService)

  implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(SessionKeys.vatNumberKey -> testVatNumber)

  "Calling the resolve action" when {
    "user has no VAT number in session" should {
      "redirect to capture VAT number" in {
        mockAuthNinoRetrieval(Some(testNino))

        val res = TestSoleTraderResolverController.resolve(FakeRequest())
        status(res) shouldBe SEE_OTHER
        redirectLocation(res) should contain(CaptureVatNumberController.show().url)
      }
    }

    "user has no nino or IRSA enrolment on profile" should {
      "redirect to the CaptureNinoController" in {
        mockAuthNinoRetrieval(None)
        val res = TestSoleTraderResolverController.resolve(request)

        status(res) shouldBe SEE_OTHER
        redirectLocation(res) should contain(routes.CaptureNinoController.show().url)
      }
    }

    "the user has a nino on their auth profile" should {
      "redirect to DirectDebitResolver" in {
        mockAuthNinoRetrieval(Some(testNino))
        mockStoreNinoSuccess(testVatNumber, testNino)

        val res = TestSoleTraderResolverController.resolve(request)

        status(res) shouldBe SEE_OTHER
        redirectLocation(res) should contain(DirectDebitResolverController.show().url)
      }
    }

    "store NINO fails" should {
      "throw an InternalServerException" in {
        mockAuthNinoRetrieval(Some(testNino))
        mockStoreNinoFailure(testVatNumber, testNino)

        intercept[InternalServerException](TestSoleTraderResolverController.resolve(request))
      }
    }
  }
}
