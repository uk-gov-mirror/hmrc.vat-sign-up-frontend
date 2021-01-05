/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockVatControllerComponents
import uk.gov.hmrc.vatsignupfrontend.forms.VatNumberForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._

class CaptureVatNumberControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockVatControllerComponents {

  object TestCaptureVatNumberController extends CaptureVatNumberController

  lazy val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/vat-number")

  def testPostRequest(vatNumberVal: String): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/vat-number").withFormUrlEncodedBody(vatNumber -> vatNumberVal)

  "Calling the show action of the Capture Vat Number controller" should {
    "go to the Capture Vat number page" in {
      mockAuthRetrieveAgentEnrolment()

      val result = TestCaptureVatNumberController.show(testGetRequest)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }


  "Calling the submit action of the Capture Vat Number controller" when {
    "form successfully submitted" should {
      "go to the new page" in {
        mockAuthRetrieveAgentEnrolment()

        val request = testPostRequest(testVatNumber)

        val result = TestCaptureVatNumberController.submit(request)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.ConfirmVatNumberController.show().url)

        session(result).get(SessionKeys.vatNumberKey) shouldBe Some(testVatNumber)
      }
    }

    "form unsuccessfully submitted" should {
      "reload the page with errors" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestCaptureVatNumberController.submit(testPostRequest("invalid"))
        status(result) shouldBe Status.BAD_REQUEST
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }
}