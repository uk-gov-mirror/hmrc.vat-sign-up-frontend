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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent.partnerships

import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.JointVenturePropertyJourney
import uk.gov.hmrc.vatsignupfrontend.controllers.ControllerSpec
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{AgentJointVentureOrProperty => Messages, Base => BaseMessages}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.YesNoMapping._
import uk.gov.hmrc.vatsignupfrontend.forms.JointVentureOrPropertyForm.yesNo
import uk.gov.hmrc.vatsignupfrontend.SessionKeys

class JointVenturePropertyControllerSpec extends ControllerSpec {

  object TestJointVenturePropertyController extends JointVenturePropertyController(mockControllerComponents)

  lazy val testGetRequest = FakeRequest("GET", "/joint-venture-or-property-partnership")

  lazy val testPostRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("POST", "/joint-venture-or-property-partnership")

  private def testPostRequest(answer: String) = {
    FakeRequest("POST", "/joint-venture-or-property-partnership").withFormUrlEncodedBody(yesNo -> answer)
      .withSession(SessionKeys.partnershipSautrKey -> "utr")
  }

  "Calling the show action of Joint Venture Property controller" when {
    "Joint venture property journey is enabled" should {
      "return an OK" in {
        enable(JointVenturePropertyJourney)
        mockAuthRetrieveAgentEnrolment()

        val result = await(TestJointVenturePropertyController.show(testGetRequest))
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "Joint venture property journey is disabled" should {
      "return NOT_FOUND" in {
        intercept[NotFoundException](await(TestJointVenturePropertyController.show(testGetRequest)))
      }
    }
  }

  "Calling the submit action of Joint Venture Property controller" when {
    "Joint venture property journey is enabled" when {
      "Yes is submitted" should {
        "redirect to Check Your Answers page" in {
          enable(JointVenturePropertyJourney)
          mockAuthRetrieveAgentEnrolment()

          val result = await(TestJointVenturePropertyController.submit(testPostRequest(option_yes)))

          session(result).get(SessionKeys.partnershipSautrKey) shouldBe None
          session(result).get(SessionKeys.jointVentureOrPropertyKey) shouldBe Some("true")
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CheckYourAnswersPartnershipController.show().url)
        }
      }

      "No is submitted" should {
        "redirect to Capture Partnership UTR page" in {
          enable(JointVenturePropertyJourney)
          mockAuthRetrieveAgentEnrolment()

          val result = await(TestJointVenturePropertyController.submit(testPostRequest(option_no)))

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CapturePartnershipUtrController.show().url)
          session(result).get(SessionKeys.jointVentureOrPropertyKey) shouldBe Some(false.toString)
        }
      }

      "An error is submitted" should {
        "return status BAD_REQUEST" in {
          enable(JointVenturePropertyJourney)
          mockAuthRetrieveAgentEnrolment()

          val result = await(TestJointVenturePropertyController.submit(testPostRequest("")))

          status(result) shouldBe Status.BAD_REQUEST
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }

    "Joint venture property journey is disabled" should {
      "return NOT_FOUND" in {
        intercept[NotFoundException](await(TestJointVenturePropertyController.submit(testPostRequest(option_yes))))
      }
    }
  }
}
