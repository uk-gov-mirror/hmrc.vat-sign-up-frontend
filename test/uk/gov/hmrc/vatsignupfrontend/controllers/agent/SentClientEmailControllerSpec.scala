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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.FinalCheckYourAnswer
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._

class SentClientEmailControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestSentClientEmailController extends SentClientEmailController(mockControllerComponents)

  lazy val testGetRequest = FakeRequest("GET", "/verify-email")

  lazy val testPostRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/verify-email")

  "Calling the show action of the verify Email controller" when {
    "there is a email in the session" should {
      "show the verify Email page" in {
        mockAuthRetrieveAgentEnrolment()
        val request = testGetRequest.withSession(SessionKeys.emailKey -> testEmail)

        val result = TestSentClientEmailController.show(request)
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "there isn't a email in the session" should {
      "redirect to Capture Email page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestSentClientEmailController.show(testGetRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureClientEmailController.show().url)
      }
    }
  }

  "Calling the submit action of the Verify Email controller" when {
    "email is in session" should {
      "redirect to AgentSendYourApplication of participation page" when {
        "the final check your answer feature switch is disabled" in {
          disable(FinalCheckYourAnswer)
          mockAuthRetrieveAgentEnrolment()

          val result = TestSentClientEmailController.submit(testPostRequest.withSession(SessionKeys.emailKey -> testEmail))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.AgentSendYourApplicationController.show().url)
        }
      }
      "redirect to the final check your answer page" when {
        "the final check your answer feature switch is enabled" in {
          enable(FinalCheckYourAnswer)
          mockAuthRetrieveAgentEnrolment()

          val result = TestSentClientEmailController.submit(testPostRequest.withSession(SessionKeys.emailKey -> testEmail))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CheckYourAnswersFinalController.show().url)
        }
      }
    }

    "email is not in session" should {
      "redirect to Capture Email page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestSentClientEmailController.submit(testPostRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureClientEmailController.show().url)
      }
    }
  }

}
