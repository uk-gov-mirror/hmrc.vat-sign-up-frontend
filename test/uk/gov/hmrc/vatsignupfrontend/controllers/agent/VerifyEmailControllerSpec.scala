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
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._

class VerifyEmailControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestVerifyEmailController extends VerifyEmailController(mockControllerComponents)

  lazy val testGetRequest = FakeRequest("GET", "/verify-email")

  lazy val testPostRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/verify-email")

  "Calling the show action of the verify Email controller" when {
    "there is a email in the session" should {
      "show the verify Email page" in {
        mockAuthRetrieveAgentEnrolment()
        val request = testGetRequest.withSession(SessionKeys.emailKey -> testEmail)

        val result = TestVerifyEmailController.show(request)
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "there isn't a email in the session" should {
      "redirect to Capture Email page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestVerifyEmailController.show(testGetRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureEmailController.show().url)
      }
    }
  }

  "Calling the submit action of the Verify Email controller" when {
    "email is in session" should {
      "redirect to Terms of participation page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestVerifyEmailController.submit(testPostRequest.withSession(SessionKeys.emailKey -> testEmail))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.TermsController.show().url)
      }
    }

    "email is not in session" should {
      "redirect to Capture Email page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestVerifyEmailController.submit(testPostRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureEmailController.show().url)
      }
    }
  }

}
