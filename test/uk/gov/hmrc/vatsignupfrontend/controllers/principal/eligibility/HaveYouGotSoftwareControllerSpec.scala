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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.eligibility

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers._
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.forms.HaveYouGotSoftwareForm
import uk.gov.hmrc.vatsignupfrontend.models.{AccountingSoftware, HaveSoftware, Neither, Spreadsheets}
import uk.gov.hmrc.vatsignupfrontend.utils.MaterializerSupport
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.eligibility.have_you_got_software
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.HaveSoftwareMapping
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{PrincipalHaveYouGotSoftware => messages}
import org.jsoup.Jsoup

class HaveYouGotSoftwareControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents with MaterializerSupport {

  object TestHaveYouGotSoftwareController extends HaveYouGotSoftwareController(mockControllerComponents)

  val testGetRequest = FakeRequest("GET", "/interruption/have-you-got-software")

  def testPostRequest(answer: HaveSoftware): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/interruption/have-you-got-software")
      .withFormUrlEncodedBody(
        HaveSoftwareMapping unbind("software", answer) head
      )

  def testPostRequestError: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("POST", "/interruption/have-you-got-software")

  "show" should {
    "render the Have you got software page" in {
      val result = await(TestHaveYouGotSoftwareController.show(testGetRequest))

      status(result) shouldBe OK
      bodyOf(result) shouldBe(
        have_you_got_software(
          haveSoftwareForm = HaveYouGotSoftwareForm.haveYouGotSoftwareForm,
          postAction = routes.HaveYouGotSoftwareController.submit()
        )(testGetRequest, mockMessagesApi.preferred(testGetRequest), mockAppConfig).body
      )
    }
  }

  "submit" when {
    "nothing has been selected" should {
      "render with errors" in {
        val result = await(TestHaveYouGotSoftwareController.submit(testPostRequestError))
        val doc = Jsoup.parse(bodyOf(result))

        status(result) shouldBe BAD_REQUEST
        doc.select("#error-message-software").text shouldBe messages.error
      }
    }
    "the answer is 'I use accounting software'" should {
      "redirect to the 'Got software' page" in {
        val result = await(TestHaveYouGotSoftwareController.submit(testPostRequest(AccountingSoftware)))

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.GotSoftwareController.show().url)
      }
    }
    "the answer is 'I use spreadsheets'" should {
      "redirect to the 'Use spreadsheets' page" in {
        val result = await(TestHaveYouGotSoftwareController.submit(testPostRequest(Spreadsheets)))

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.UseSpreadsheetsController.show().url)
      }
    }
    "the answer is 'I use neither'" should {
      "redirect to the 'Not got software' page" in {
        val result = await(TestHaveYouGotSoftwareController.submit(testPostRequest(Neither)))

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.NotGotSoftwareController.show().url)
      }
    }
  }

}
