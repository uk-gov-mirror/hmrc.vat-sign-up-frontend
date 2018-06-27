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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.CtKnownFactsIdentityVerification
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.forms.CompanyUtrForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._

class CaptureCompanyUtrControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(CtKnownFactsIdentityVerification)
  }

  object TestCaptureCompanyUtrController extends CaptureCompanyUtrController(mockControllerComponents)

  lazy val testGetRequest = FakeRequest("GET", "/company-utr")

  def testPostRequest(companyUtrVal: String): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/company-utr").withFormUrlEncodedBody(companyUtr -> companyUtrVal)

  "Calling the show action of the Capture Company Utr controller" should {
    "go to the Capture Company Utr page" in {
      mockAuthAdminRole()

      val result = TestCaptureCompanyUtrController.show(testGetRequest)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "Calling the submit action of the Capture Company Utr controller" should {
    "goto confirm Company number page" in {
      mockAuthAdminRole()

      val request = testPostRequest(testCompanyUtr)

      val result = TestCaptureCompanyUtrController.submit(request)

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.NoCtEnrolmentSummaryController.show().url)

      result.session(request).get(SessionKeys.companyUtrKey) shouldBe Some(testCompanyUtr)
    }
  }

  "form unsuccessfully submitted" should {
    "reload the page with errors" in {
      mockAuthAdminRole()

      val result = TestCaptureCompanyUtrController.submit(testPostRequest("invalid"))
      status(result) shouldBe Status.BAD_REQUEST
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

}
