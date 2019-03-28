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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships

import play.api.http.Status
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.OptionalSautrJourney
import uk.gov.hmrc.vatsignupfrontend.controllers.ControllerSpec
import uk.gov.hmrc.vatsignupfrontend.forms.JointVentureOrPropertyForm._
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.YesNoMapping

class JointVentureOrPropertyControllerSpec extends ControllerSpec {

  object TestJointVentureOrPropertyController extends JointVentureOrPropertyController(mockControllerComponents)

  val testGetRequest = FakeRequest("GET", "/joint-venture-or-property-partnership")

  def testPostRequest(answer: String): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/joint-venture-or-property-partnership").withFormUrlEncodedBody(yesNo -> answer)

  "Calling the show action of the Joint Venture or Property controller" when {
    "The Joint Venture journey is enabled" should {
      "return OK" in {
        enable(OptionalSautrJourney)
        mockAuthAdminRole()

        lazy val result = await(TestJointVentureOrPropertyController.show(testGetRequest))

        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "The Joint Venture journey is disabled" should {
      "return NOT_FOUND" in {
        disable(OptionalSautrJourney)
        mockAuthAdminRole()

        intercept[NotFoundException](await(TestJointVentureOrPropertyController.submit(
          testPostRequest(answer = YesNoMapping.option_no)
        )))
      }
    }
  }

  "Calling the submit action of the Joint Venture or Property controller" when {
    "The Joint Venture journey is enabled" when {
      "form successfully submitted" when {
        "the choice is YES" should {
          "redirect to CheckYourAnswersPartnership" in {
            enable(OptionalSautrJourney)
            mockAuthAdminRole()

            val result = await(TestJointVentureOrPropertyController.submit(
              testPostRequest(answer = YesNoMapping.option_yes).withSession(
                SessionKeys.partnershipSautrKey -> "utr",
                SessionKeys.businessPostCodeKey -> "postcode"
              )
            ))

            status(result) shouldBe Status.SEE_OTHER
            session(result).get(SessionKeys.partnershipSautrKey) shouldBe None
            session(result).get(SessionKeys.businessPostCodeKey) shouldBe None
            session(result).get(SessionKeys.jointVentureOrPropertyKey) shouldBe Some(true.toString)
            redirectLocation(result) shouldBe Some(routes.CheckYourAnswersPartnershipsController.show().url)
          }
        }
        "the choice is NO" should {
          "redirect to CapturePartnershipUtr" in {
            enable(OptionalSautrJourney)
            mockAuthAdminRole()

            val result = await(TestJointVentureOrPropertyController.submit(
              testPostRequest(answer = YesNoMapping.option_no)
            ))

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.CapturePartnershipUtrController.show().url)
            session(result).get(SessionKeys.jointVentureOrPropertyKey) shouldBe Some(false.toString)
          }
        }
      }
      "form submitted with errors" should {
        "return BAD_REQUEST" in {
          enable(OptionalSautrJourney)
          mockAuthAdminRole()

          val result = await(TestJointVentureOrPropertyController.submit(testPostRequest("")))

          status(result) shouldBe Status.BAD_REQUEST
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }

    "The Joint Venture journey is disabled" should {
      "return NOT_FOUND" in {
        disable(OptionalSautrJourney)

        intercept[NotFoundException](await(TestJointVentureOrPropertyController.submit(
          testPostRequest(answer = YesNoMapping.option_no)
        )))
      }
    }
  }
}
