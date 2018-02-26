/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.vatsubscriptionfrontend.controllers

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys
import uk.gov.hmrc.vatsubscriptionfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.forms.CompanyNumberForm._

import scala.concurrent.Future
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.TestConstants._

class CaptureCompanyNumberControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestCaptureCompanyNumberController extends CaptureCompanyNumberController(mockControllerComponents)

  lazy val testGetRequest = FakeRequest("GET", "/company-number")

  def testPostRequest(companyNumberVal: String): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/company-number").withFormUrlEncodedBody(companyNumber -> companyNumberVal)

  "Calling the show action of the Capture Company Number controller" should {
    "go to the Capture Company number page" in {
      mockAuthRetrieveAgentEnrolment()

      val result = TestCaptureCompanyNumberController.show(testGetRequest)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }


  "Calling the submit action of the Capture Company Number controller" when {
    "form successfully submitted" should {
      "go to the new page" in {
        mockAuthRetrieveAgentEnrolment()

        val request = testPostRequest(testCompanyNumber)

        val result = TestCaptureCompanyNumberController.submit(request)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.ConfirmCompanyNumberController.show().url)

        result.session(request).get(SessionKeys.companyNumberKey) shouldBe Some(testCompanyNumber)
      }
    }

    "form unsuccessfully submitted" should {
      "reload the page with errors" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestCaptureCompanyNumberController.submit(testPostRequest("invalid"))
        status(result) shouldBe Status.BAD_REQUEST
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }
}