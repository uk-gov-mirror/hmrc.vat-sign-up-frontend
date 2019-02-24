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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.AdditionalKnownFacts
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.forms.Box5FigureForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._

class CaptureBox5FigureControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(AdditionalKnownFacts)
  }

  object TestCaptureBox5FigureController extends CaptureBox5FigureController(mockControllerComponents)

  lazy val testGetRequest = FakeRequest("GET", "/box-5-figure")

  lazy val testPostRequest = FakeRequest("POST", "/box-5-figure").withFormUrlEncodedBody(box5Figure -> testBox5Figure)

  "calling the show method on CaptureBox5FigureController" when {
    "the AdditionalKnownFacts feature switch is enabled" should {
      "go to the capture box five value page" in {
        mockAuthAdminRole()

        val result = TestCaptureBox5FigureController.show(testGetRequest)
        status(result) shouldBe OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "the AdditionalKnownFacts switch is off" should {
      "throw not found exception" in {
        disable(AdditionalKnownFacts)

        intercept[NotFoundException] {
          await(TestCaptureBox5FigureController.show(testGetRequest))
        }
      }
    }
  }

  "calling the submit method on CaptureBox5FigureController" when {
    "the AdditionalKnownFacts feature switch is on" should {
      "redirect to CaptureLastMonthReturnPeriod page" in {
        mockAuthAdminRole()

        val result = await(TestCaptureBox5FigureController.submit(testPostRequest))

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureLastReturnMonthPeriodController.show().url)
        result.session(testPostRequest).get(SessionKeys.box5FigureKey) should contain(testBox5Figure)
      }
      "form unsuccessfully submitted" should {
        "reload the page with errors" in {
          val testPostRequest = FakeRequest("POST", "/box-5-figure")

          mockAuthAdminRole()

          val result = TestCaptureBox5FigureController.submit(testPostRequest)
          status(result) shouldBe BAD_REQUEST
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }
  }

}
