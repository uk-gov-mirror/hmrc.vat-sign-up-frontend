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
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{Base => BaseMessages}
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{PrincipalJointVentureOrProperty => Messages}
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.JointVenturePropertyJourney
import uk.gov.hmrc.vatsignupfrontend.controllers.ControllerSpec
import uk.gov.hmrc.vatsignupfrontend.forms.HaveSoftwareForm._
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.YesNoMapping


class JointVentureOrPropertyControllerSpec extends ControllerSpec {

  object TestJointVentureOrPropertyController extends JointVentureOrPropertyController(mockControllerComponents)

  val testGetRequest = FakeRequest("GET", "/joint-venture-or-property-partnership")

  def testPostRequest(answer: String): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/joint-venture-or-property-partnership").withFormUrlEncodedBody(yesNo -> answer)

  "Calling the show action of the Joint Venture or Property controller" should {

    lazy val result = await(TestJointVentureOrPropertyController.show(testGetRequest))

    "user is authenticated and journey enabled" should {

      "return status OK (200)" in {
        mockAuthAdminRole()
        enable(JointVenturePropertyJourney)
        status(result) shouldBe Status.OK
      }

      "return content type html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "return charset utf-8" in {
        charset(result) shouldBe Some("utf-8")
      }

      "render the Joint Venture or Property view" in {
        titleOf(result) shouldBe Messages.title
      }
    }

    "the journey is disabled" should {

      "return status NOT_FOUND (404)" in {
        disable(JointVenturePropertyJourney)
        lazy val result = TestJointVentureOrPropertyController.submit(testPostRequest(answer = YesNoMapping.option_no))
        intercept[NotFoundException](result)
      }
    }
  }

  "Calling the submit action of the Joint Venture or Property controller" when {

    "user is authenticated" when {

      "form successfully submitted" when {

        "the choice is YES" should {

          lazy val result = TestJointVentureOrPropertyController.submit(testPostRequest(answer = YesNoMapping.option_yes)
            .withSession(SessionKeys.partnershipSautrKey -> "utr")
          )

          "return status SEE_OTHER (303)" in {
            mockAuthAdminRole()
            enable(JointVenturePropertyJourney)
            status(result) shouldBe Status.SEE_OTHER
          }

          "remove partnershipSautrKey from session" in {
            session(result).get(SessionKeys.partnershipSautrKey) shouldBe None
          }

          s"redirect to '${routes.CheckYourAnswersPartnershipsController.show().url}'" in {
            redirectLocation(result) shouldBe Some(routes.CheckYourAnswersPartnershipsController.show().url)
          }
        }

        "the choice is NO" should {

          lazy val result = TestJointVentureOrPropertyController.submit(testPostRequest(answer = YesNoMapping.option_no))

          "return status SEE_OTHER (303)" in {
            mockAuthAdminRole()
            enable(JointVenturePropertyJourney)
            status(result) shouldBe Status.SEE_OTHER
          }

          s"redirect to '${routes.CapturePartnershipUtrController.show().url}'" in {
            redirectLocation(result) shouldBe Some(routes.CapturePartnershipUtrController.show().url)
          }
        }
      }

      "form submitted with errors" should {

        lazy val result = TestJointVentureOrPropertyController.submit(testPostRequest(""))

        "return status BAD_REQUEST (400)" in {
          mockAuthAdminRole()
          enable(JointVenturePropertyJourney)
          status(result) shouldBe Status.BAD_REQUEST
        }

        "return content type html" in {
          contentType(result) shouldBe Some("text/html")
        }

        "return charset utf-8" in {
          charset(result) shouldBe Some("utf-8")
        }

        "render the Joint Venture or Property view" in {
          titleOf(result) shouldBe BaseMessages.errPrefix + " " + Messages.title
        }
      }
    }

    "the journey is disabled" should {

      "return status NOT_FOUND (404)" in {
        disable(JointVenturePropertyJourney)
        lazy val result = TestJointVentureOrPropertyController.submit(testPostRequest(answer = YesNoMapping.option_no))
        intercept[NotFoundException](result)
      }
    }
  }
}
