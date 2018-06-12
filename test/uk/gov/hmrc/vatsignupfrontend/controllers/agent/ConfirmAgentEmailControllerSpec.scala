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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStoreEmailAddressService

class ConfirmAgentEmailControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents
  with MockStoreEmailAddressService {

  object TestConfirmAgentEmailController extends ConfirmAgentEmailController(mockControllerComponents, mockStoreEmailAddressService)

  lazy val testGetRequest = FakeRequest("GET", "/confirm-your-email")

  lazy val testPostRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/confirm-your-email")

  "Calling the show action of the Confirm Agent Email controller" when {
    "there is a email in the session" should {
      "show the Confirm Your Email page" in {
        mockAuthRetrieveAgentEnrolment()
        val request = testGetRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber, SessionKeys.emailKey -> testEmail)

        val result = TestConfirmAgentEmailController.show(request)

        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "there isn't a email in the session" should {
      "redirect to Capture Agent Email page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestConfirmAgentEmailController.show(testGetRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureAgentEmailController.show().url)
      }
    }
  }

  "Calling the submit action of the Confirm Agent Email controller" when {
    "email and vat number is in session and store call is successful" when {
      "email is not verified" should {
        "redirect to Verify Agent Email page" in {
          mockAuthRetrieveAgentEnrolment()
          mockStoreEmailAddressSuccess(vatNumber = testVatNumber, email = testEmail)(emailVerified = false)

          val result = TestConfirmAgentEmailController.submit(testPostRequest.withSession(SessionKeys.emailKey -> testEmail,
            SessionKeys.vatNumberKey -> testVatNumber))
//TODO: create verify agent email controller
          status(result) shouldBe Status.NOT_IMPLEMENTED
          //redirectLocation(result) shouldBe Some(routes.VerifyAgentEmailController.show().url)
        }
      }
      "email is verified" should {
        "redirect to Terms page" in {
          mockAuthRetrieveAgentEnrolment()
          mockStoreEmailAddressSuccess(vatNumber = testVatNumber, email = testEmail)(emailVerified = true)

          val result = TestConfirmAgentEmailController.submit(testPostRequest.withSession(SessionKeys.emailKey -> testEmail,
            SessionKeys.vatNumberKey -> testVatNumber))

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TermsController.show().url)
        }
      }
    }
    "email and vat number is in session but store call is unsuccessful" should {
      "throw Internal Server Error" in {
        mockAuthRetrieveAgentEnrolment()
        mockStoreEmailAddressFailure(vatNumber = testVatNumber, email = testEmail)

        intercept[InternalServerException] {
          await(TestConfirmAgentEmailController.submit(testPostRequest.withSession(
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.emailKey -> testEmail
          )))
        }
      }
    }
    "vat number is not in session" should {
      "redirect to Capture Vat number page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestConfirmAgentEmailController.submit(testPostRequest.withSession(SessionKeys.emailKey -> testEmail))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureVatNumberController.show().url)
      }
    }
    "email is not in session" should {
      "redirect to Capture Agent Email page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestConfirmAgentEmailController.submit(testPostRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureAgentEmailController.show().url)
      }
    }
  }

}
