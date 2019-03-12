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

import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{AgentSendYourApplication, Terms}
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.SendYourApplication
import uk.gov.hmrc.vatsignupfrontend.controllers.ControllerSpec
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockSubmissionService

class TermsControllerSpec extends ControllerSpec with MockSubmissionService {

  object TestTermsController extends TermsController(mockControllerComponents, mockSubmissionService)

  lazy val testGetRequest = FakeRequest("GET", "/terms-of-participation")

  lazy val testPostRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/terms-of-participation").withSession(SessionKeys.vatNumberKey -> testVatNumber)

  "Calling the show action of the Terms controller" when {

    "SendYourApplication is disabled" should {

      val request = testGetRequest

      lazy val result = TestTermsController.show(request)

      "return an OK" in {
        mockAuthRetrieveAgentEnrolment()
        status(result) shouldBe Status.OK
      }

      "return Html" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      "display the correct page" in {
        titleOf(result) shouldBe Terms.title
      }
    }

    "SendYourApplication is enabled" should {

      val request = testGetRequest

      lazy val result = TestTermsController.show(request)

      "return an OK" in {
        enable(SendYourApplication)
        mockAuthRetrieveAgentEnrolment()
        status(result) shouldBe Status.OK
      }

      "return Html" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      "display the correct page" in {
        titleOf(result) shouldBe AgentSendYourApplication.title
      }
    }
  }

  "Calling the submit action of the Terms controller" when {
    "submission is successful" should {
      "goto information recieved page" in {
        mockAuthRetrieveAgentEnrolment()
        mockSubmitSuccess(testVatNumber)

        val result = TestTermsController.submit(testPostRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.ConfirmationController.show().url)
      }
    }
    "submission is unsuccessful" should {
      "throw internal server exception" in {
        mockAuthRetrieveAgentEnrolment()
        mockSubmitFailure(testVatNumber)

        intercept[InternalServerException] {
          await(TestTermsController.submit(testPostRequest))
        }
      }
    }
  }

}
