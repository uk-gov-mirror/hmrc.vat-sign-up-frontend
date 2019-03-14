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

  "Calling the show action of Joint Venture Property controller" should {

    "Joint venture property journey is enabled" should {

      lazy val result = TestJointVenturePropertyController.show(testGetRequest)

      "return status OK (200)" in {
        mockAuthRetrieveAgentEnrolment()
        enable(JointVenturePropertyJourney)
        status(result) shouldBe Status.OK
      }

      "return Html" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      "display the correct page" in {
        titleOf(result) shouldBe Messages.title
      }
    }

    "Joint venture property journey is disabled" should {

      lazy val result = TestJointVenturePropertyController.show(testGetRequest)

      "return status NOT_FOUND (404)" in {
        intercept[NotFoundException](result)
      }
    }
  }

  "Calling the submit action of Joint Venture Property controller" when {

    "Joint venture property journey is enabled" when {

      "Yes is submitted" should {

        lazy val result = TestJointVenturePropertyController.submit(testPostRequest(option_yes))

        "return status SEE_OTHER (303)" in {
          mockAuthRetrieveAgentEnrolment()
          enable(JointVenturePropertyJourney)
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect to Check Your Answers Partnership page" in {
          redirectLocation(result) shouldBe Some(routes.CheckYourAnswersPartnershipController.show().url)
        }

        s"remove ${SessionKeys.partnershipSautrKey} from session" in {
          session(result).get(SessionKeys.partnershipSautrKey) shouldBe None
        }

        s"add ${SessionKeys.jointVentureOrPropertyKey} = $option_yes to session" in {
          session(result).get(SessionKeys.jointVentureOrPropertyKey) shouldBe Some("true")
        }
      }

      "No is submitted" should {

        lazy val result = TestJointVenturePropertyController.submit(testPostRequest(option_no))

        "return status SEE_OTHER (303)" in {
          mockAuthRetrieveAgentEnrolment()
          enable(JointVenturePropertyJourney)
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect to Capture Partnership UTR page" in {
          redirectLocation(result) shouldBe Some(routes.CapturePartnershipUtrController.show().url)
        }

        s"add ${SessionKeys.jointVentureOrPropertyKey} = $option_no to session" in {
          session(result).get(SessionKeys.jointVentureOrPropertyKey) shouldBe Some(false.toString)
        }
      }

      "An error is submitted" should {

        lazy val result = TestJointVenturePropertyController.submit(testPostRequest("how?"))

        "return status BAD_REQUEST (400)" in {
          mockAuthRetrieveAgentEnrolment()
          enable(JointVenturePropertyJourney)
          status(result) shouldBe Status.BAD_REQUEST
        }

        "return Html" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "display the correct page" in {
          titleOf(result) shouldBe s"${BaseMessages.errPrefix} ${Messages.title}"
        }
      }
    }

    "Joint venture property journey is disabled" should {

      lazy val result = TestJointVenturePropertyController.submit(testPostRequest(option_yes))

      "return status NOT_FOUND (404)" in {
        intercept[NotFoundException](result)
      }
    }
  }
}
