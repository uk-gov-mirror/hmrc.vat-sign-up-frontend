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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.models.UserEntered
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStoreNinoService

import scala.concurrent.Future

class ConfirmClientDetailsControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents
  with MockStoreNinoService {

  object TestConfirmClientDetailsController extends ConfirmClientDetailsController(mockControllerComponents, mockStoreNinoService)

  lazy val testGetRequest = FakeRequest("GET", "/confirm-client")

  lazy val testPostRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/confirm-client")


  "Calling the show action of the Confirm Client Details controller" when {
    "there is a vrn and clientDetails in the session" should {
      "NOT_IMPLEMENTED" in {
        mockAuthRetrieveAgentEnrolment()
        val request = testGetRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber, SessionKeys.userDetailsKey -> testUserDetailsJson)

        val result = TestConfirmClientDetailsController.show(request)
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "there isn't a vrn in the session" should {
      "redirect to Capture Vat Number page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestConfirmClientDetailsController.show(testGetRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureVatNumberController.show().url)
      }
    }

    "there isn't a user detail in the session" should {
      "redirect to Capture Client Details page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestConfirmClientDetailsController.show(testGetRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureClientDetailsController.show().url)
      }
    }
  }

  "Calling the submit action of the Confirm Client Details controller" when {
    "vat number and client details are in session" when {
      lazy val request = testPostRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber, SessionKeys.userDetailsKey -> testUserDetailsJson)

      def callSubmit: Future[Result] = TestConfirmClientDetailsController.submit(request)

      "and store nino is successful" should {
        "go to the capture agent email page" in {
          mockAuthRetrieveAgentEnrolment()
          mockStoreNinoSuccess(testVatNumber, testUserDetails.nino, UserEntered)

          val result = callSubmit

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CaptureAgentEmailController.show().url)

          result.session(request).get(SessionKeys.userDetailsKey) shouldBe None
        }
      }

      "but store nino returned no match" should {
        "throw internal server exception" in {
          mockAuthRetrieveAgentEnrolment()
          mockStoreNinoNoMatch(testVatNumber, testUserDetails.nino, UserEntered)

          val result = callSubmit

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.FailedClientMatchingController.show().url)

          result.session(request).get(SessionKeys.userDetailsKey) shouldBe None
        }
      }

      "but store nino returned no vat" should {
        "throw internal server exception" in {
          mockAuthRetrieveAgentEnrolment()
          mockStoreNinoNoVatStored(testVatNumber, testUserDetails.nino, UserEntered)

          val result = callSubmit

          intercept[InternalServerException] {
            await(result)
          }
        }
      }

      "but store nino returned failure" should {
        "throw internal server exception" in {
          mockAuthRetrieveAgentEnrolment()
          mockStoreNinoNoVatStored(testVatNumber, testUserDetails.nino, UserEntered)

          val result = callSubmit

          intercept[InternalServerException] {
            await(result)
          }
        }
      }
    }

    "vat number is not in session" should {
      "redirect to capture vat number" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestConfirmClientDetailsController.submit(testPostRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureVatNumberController.show().url)
      }
    }

    "client details is not in session" should {
      "redirect to capture client details" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestConfirmClientDetailsController.submit(testPostRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureClientDetailsController.show().url)
      }
    }
  }

}