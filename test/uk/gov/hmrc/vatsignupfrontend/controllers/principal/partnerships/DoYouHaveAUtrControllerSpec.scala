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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships

import play.api.http.Status
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.OptionalSautrJourney
import uk.gov.hmrc.vatsignupfrontend.controllers.ControllerSpec
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants.{testBusinessPostcode, testSaUtr}

class DoYouHaveAUtrControllerSpec extends ControllerSpec {

  object TestDoYouHaveAUtrController extends DoYouHaveAUtrController

  val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/do-you-have-a-utr")

  def testPostRequest(answer: String): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/do-you-have-a-utr").withFormUrlEncodedBody("yes_no" -> answer)

  "Calling the show action of the Do You Have A Utr Controller" when {
    "The feature switch is enabled" should {
      "return OK" in {
        enable(OptionalSautrJourney)
        mockAuthAdminRole()

        lazy val result = TestDoYouHaveAUtrController.show(testGetRequest)

        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "The feature switch is disabled" should {
      "return NOT_FOUND" in {
        disable(OptionalSautrJourney)
        mockAuthAdminRole()

        intercept[NotFoundException](TestDoYouHaveAUtrController.show(testGetRequest))
      }
    }
  }

  "Calling the submit action of the Do You Have A Utr Controller" when {
    "The feature switch is enabled" when {
      "form successfully submitted" when {
        "the choice is yes" should {
          "redirect to CapturePartnershipUtr" in {
            enable(OptionalSautrJourney)
            mockAuthAdminRole()

            val result = TestDoYouHaveAUtrController.submit(
              testPostRequest(answer = YesNoMapping.option_yes)
            )

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.CapturePartnershipUtrController.show().url)
            session(result).get(SessionKeys.hasOptionalSautrKey) shouldBe Some(true.toString)
          }
        }

        "the choice is No" should {
          "redirect to CheckYourAnswersPartnership" in {
            enable(OptionalSautrJourney)
            mockAuthAdminRole()

            val result = TestDoYouHaveAUtrController.submit(
              testPostRequest(answer = YesNoMapping.option_no).withSession(
                SessionKeys.partnershipSautrKey -> testSaUtr,
                SessionKeys.partnershipPostCodeKey -> testBusinessPostcode.postCode
              )
            )

            status(result) shouldBe Status.SEE_OTHER
            session(result).get(SessionKeys.partnershipSautrKey) shouldBe None
            session(result).get(SessionKeys.partnershipPostCodeKey) shouldBe None
            session(result).get(SessionKeys.hasOptionalSautrKey) shouldBe Some(false.toString)
            redirectLocation(result) shouldBe Some(routes.CheckYourAnswersPartnershipsController.show().url)
          }
        }
      }

      "form submitted with errors" should {
        "return BAD_REQUEST" in {
          enable(OptionalSautrJourney)
          mockAuthAdminRole()

          val result = TestDoYouHaveAUtrController.submit(testPostRequest(""))

          status(result) shouldBe Status.BAD_REQUEST
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }

    "The feature switch is disabled" should {
      "return NOT_FOUND" in {
        disable(OptionalSautrJourney)

        intercept[NotFoundException](TestDoYouHaveAUtrController.submit(
          testPostRequest(answer = YesNoMapping.option_no)
        ))
      }
    }
  }
}
