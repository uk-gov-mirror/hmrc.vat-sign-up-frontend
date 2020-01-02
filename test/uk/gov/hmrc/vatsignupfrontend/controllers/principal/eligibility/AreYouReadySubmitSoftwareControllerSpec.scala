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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.eligibility

import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.eligibility.are_you_ready_submit_software
import uk.gov.hmrc.vatsignupfrontend.forms.eligibility.AreYouReadySubmitSoftwareForm._
import play.api.i18n.Messages.Implicits._
import play.api.mvc.AnyContentAsFormUrlEncoded
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.YesNoMapping._


class AreYouReadySubmitSoftwareControllerSpec extends UnitSpec with MockControllerComponents {

  object TestAreYouReadySubmitSoftwareController extends AreYouReadySubmitSoftwareController(mockControllerComponents)

  "The show method" should {
    "render the are_you_ready_submit_software view" in {
      implicit val testGetRequest = FakeRequest("GET", "/are-you-ready-to-submit")

      val result = await(TestAreYouReadySubmitSoftwareController.show(testGetRequest))

      status(result) shouldBe OK
      contentAsString(result) shouldBe are_you_ready_submit_software(areYouReadySubmitSoftwareForm, routes.AreYouReadySubmitSoftwareController.submit()).body
    }
  }

  "The submit method" should {
    def testPostRequest(entityTypeVal: String): FakeRequest[AnyContentAsFormUrlEncoded] =
      FakeRequest("POST", "/are-you-ready-to-submit").withFormUrlEncodedBody(yesNo -> entityTypeVal)

    "reload the view with errors" when {
      "user does not click a radio button" in {
        implicit val request = testPostRequest(entityTypeVal = "")
        val result = await(TestAreYouReadySubmitSoftwareController.submit(request))

        status(result) shouldBe BAD_REQUEST
        contentAsString(result) shouldBe are_you_ready_submit_software(
          areYouReadySubmitSoftwareForm.bindFromRequest(),
          routes.AreYouReadySubmitSoftwareController.submit()
        ).body

      }
    }

    "redirect to Making Tax Digital Software Page" when {
      "user selects the Yes button" in {
        val result = await(TestAreYouReadySubmitSoftwareController.submit(testPostRequest(entityTypeVal = option_yes)))

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.MakingTaxDigitalSoftwareController.show().url)
      }
    }

    "redirect to Return Due Page" when {
      "user selects No button" in {
        val result = await(TestAreYouReadySubmitSoftwareController.submit(testPostRequest(entityTypeVal = option_no)))

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.ReturnDueController.show().url)
      }
    }
  }

}
