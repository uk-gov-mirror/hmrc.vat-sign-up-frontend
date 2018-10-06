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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.GeneralPartnership
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants.{testSaUtr, testVatNumber}


class ConfirmGeneralPartnershipControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents
  //TODO with MockStorePartnershipSautrService
{

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(GeneralPartnership)
  }

  object TestConfirmGeneralPartnershipController extends ConfirmGeneralPartnershipController(mockControllerComponents)
  // TODO mockStorePartnershipSautrService)

  val testGetRequest = FakeRequest("GET", "/confirm-partnership-utr")

  val testPostRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/confirm-partnership-utr")

  "Calling the show action of the Confirm General Partnership controller" when {
    "there is a company number in the session" should {
      "go to the Confirm Company Number page" in {
        mockAuthAdminRole()
        val request = testGetRequest.withSession(
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.partnershipSautrKey -> testSaUtr
        )

        val result = TestConfirmGeneralPartnershipController.show(request)
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
    "there isn't a vat number in the session" should {
      "go to the your vat number page" in {
        mockAuthAdminRole()

        val result = TestConfirmGeneralPartnershipController.show(testGetRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(principalRoutes.ResolveVatNumberController.resolve().url)
      }
    }
    "there isn't a partnership sautr in the session" should {
      "show technical difficulties" in {
        mockAuthAdminRole()

        intercept[InternalServerException] {
          await(TestConfirmGeneralPartnershipController.submit(testPostRequest.withSession(
            SessionKeys.vatNumberKey -> testVatNumber
          )))
        }
      }
    }
  }


  "Calling the submit action of the Confirm General Partnership controller" when {
    "vat number is in session" when {
      "go to the 'agree to receive emails' page" in {
        mockAuthAdminRole()
        // TODO mockStorePartnershipSautrSuccess(vatNumber = testVatNumber, partnershipSautr = testSaUtr)

        val result = TestConfirmGeneralPartnershipController.submit(testPostRequest.withSession(
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.partnershipSautrKey -> testSaUtr
        ))
        status(result) shouldBe Status.NOT_IMPLEMENTED // Status.SEE_OTHER
        // redirectLocation(result) shouldBe Some(routes.AgreeCaptureEmailController.show().url)
      }
    }
  }
  "vat number is in session but store vat is unsuccessful" should {
    "throw internal server exception" in {
      mockAuthAdminRole()
      // TODO mockStorePartnershipSautrFailure(vatNumber = testVatNumber, partnershipSautr = testSaUtr)

      intercept[InternalServerException] {
        await(TestConfirmGeneralPartnershipController.submit(testPostRequest.withSession(
          SessionKeys.vatNumberKey -> testVatNumber
          // SessionKeys.partnershipSautrKey -> testSaUtr
        )))
      }
    }
  }
  "vat number is not in session" should {
    "redirect to your vat number" in {
      mockAuthAdminRole()

      val result = TestConfirmGeneralPartnershipController.submit(testPostRequest)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(principalRoutes.ResolveVatNumberController.resolve().url)
    }
  }
  "partnership sautr is not in session" should {
    "show technical difficulties" in {
      mockAuthAdminRole()

      intercept[InternalServerException] {
        await(TestConfirmGeneralPartnershipController.submit(testPostRequest.withSession(
          SessionKeys.vatNumberKey -> testVatNumber
        )))
      }
    }
  }
}