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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.soletrader

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.SkipCidCheck
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStoreNinoService
import uk.gov.hmrc.vatsignupfrontend.SessionKeys.ninoKey
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants.{testNino, testVatNumber}
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.{CaptureVatNumberController, DirectDebitResolverController}
import uk.gov.hmrc.vatsignupfrontend.models.{SoleTrader, UserEntered}

class ConfirmNinoControllerSpec extends UnitSpec
                                with GuiceOneAppPerSuite
                                with MockControllerComponents
                                with MockStoreNinoService {

  object TestConfirmNinoController extends ConfirmNinoController(mockControllerComponents, mockStoreNinoService)

  val testGetRequest = FakeRequest("GET", "/confirm-national-insurance-number")
  val testPostRequest = FakeRequest("POST", "/confirm-national-insurance-number")

  override def beforeEach(): Unit = enable(SkipCidCheck)

  "Calling the Show method of Confirm NINO controller" when {
    "the SkipCidCheck feature switch is enabled" should {
      "show the Confirm NINO view" in {
        mockAuthAdminRole()

        implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] =
          testGetRequest.withSession(
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.businessEntityKey -> SoleTrader.toString,
            SessionKeys.ninoKey -> testNino
          )

        val res = await(TestConfirmNinoController.show(request))

        status(res) shouldBe OK
        contentType(res) shouldBe Some("text/html")
        charset(res) shouldBe Some("utf-8")
      }
    }

    "the SkipCidCheck feature switch is disabled" should {
      "throw a NotFoundException" in {
        disable(SkipCidCheck)
        mockAuthAdminRole()

        intercept[NotFoundException] {
          await(TestConfirmNinoController.show(testGetRequest))
        }
      }
    }
  }

  "Calling the submit method of the Confirm NINO controller" when {
    "there is a NINO and VAT number in the user's session" should {
      "Redirect to DirectDebitResolver" in {
        mockAuthAdminRole()
        mockStoreNinoSuccess(testVatNumber, testNino, UserEntered)

        implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] =
          testPostRequest.withSession(
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.businessEntityKey -> SoleTrader.toString,
            SessionKeys.ninoKey -> testNino
          )

        val res = await(TestConfirmNinoController.submit(request))

        status(res) shouldBe SEE_OTHER
        redirectLocation(res) should contain(DirectDebitResolverController.show().url)

        res.session.get(ninoKey) should contain(testNino)
        res.session.get(SessionKeys.vatNumberKey) should contain(testVatNumber)
        res.session.get(SessionKeys.businessEntityKey) should contain(SoleTrader.toString)
      }
    }

    "there is a VAT number, but no NINO in the user's session" should {
      "redirect to the Capture NINO page" in {
        mockAuthAdminRole()

        implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] =
          testPostRequest.withSession(
            SessionKeys.vatNumberKey -> testVatNumber
          )

        val res = await(TestConfirmNinoController.submit(request))

        status(res) shouldBe SEE_OTHER
        redirectLocation(res) should contain(routes.CaptureNinoController.show().url)
      }
    }

    "there is a NINO, but no VAT number in the user's session" should {
      "redirect to the Capture VAT number page" in {
        mockAuthAdminRole()

        implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] =
          testPostRequest.withSession(
            SessionKeys.ninoKey -> testNino
          )

        val res = await(TestConfirmNinoController.submit(request))

        status(res) shouldBe SEE_OTHER
        redirectLocation(res) should contain(CaptureVatNumberController.show().url)
      }
    }
  }

}
