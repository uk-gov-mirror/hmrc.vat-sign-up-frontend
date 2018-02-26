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

package uk.gov.hmrc.vatsubscriptionfrontend.controllers

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys
import uk.gov.hmrc.vatsubscriptionfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.forms.EmailForm._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.TestConstants._

class CaptureEmailControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestCaptureEmailController extends CaptureEmailController(mockControllerComponents)

  val testGetRequest = FakeRequest("GET", "/email-address")

  def testPostRequest(emailAddress: String): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/email-address").withFormUrlEncodedBody(email -> emailAddress)

  "Calling the show action of the Capture Email controller" should {
    "go to the Capture Email page" in {
      mockAuthRetrieveAgentEnrolment()

      val result = TestCaptureEmailController.show(testGetRequest)

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }


  "Calling the submit action of the Capture Email controller" when {
    "form successfully submitted" should {
      "go to the Confirm Email page" in {
        mockAuthRetrieveAgentEnrolment()

        val request = testPostRequest(testEmail)

        val result = TestCaptureEmailController.submit(request)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.ConfirmEmailController.show().url)

        await(result).session(request).get(SessionKeys.emailKey) shouldBe Some(testEmail)
      }
    }

    "form unsuccessfully submitted" should {
      "reload the page with errors" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestCaptureEmailController.submit(testPostRequest("invalid"))
        status(result) shouldBe Status.BAD_REQUEST
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }
}