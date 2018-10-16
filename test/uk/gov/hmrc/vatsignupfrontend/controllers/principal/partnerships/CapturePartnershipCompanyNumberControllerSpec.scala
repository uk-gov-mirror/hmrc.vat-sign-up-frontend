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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.LimitedPartnershipJourney
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.forms.CompanyNumberForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.models.companieshouse.LimitedPartnership
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockGetCompanyNameService

class CapturePartnershipCompanyNumberControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents
  with MockGetCompanyNameService {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(LimitedPartnershipJourney)
  }

  object TestCaptureCompanyNumberController extends CapturePartnershipCompanyNumberController(
    mockControllerComponents,
    mockGetCompanyNameService
  )

  lazy val testGetRequest = FakeRequest("GET", "/partnership-company-number")

  def testPostRequest(companyNumberVal: String): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/partnership-company-number").withFormUrlEncodedBody(companyNumber -> companyNumberVal)


  "Calling the show action of the Capture Partnership Company Number controller" should {
    "go to the Capture Partnership Company Number page" in {
      mockAuthAdminRole()

      val result = TestCaptureCompanyNumberController.show(testGetRequest)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "Calling the submit action of the Capture Partnership Company Number controller" when {
    "get company name returned successfully" should {
      "Not implemented" in {
        //TODO Redirect(routes.ConfirmPartnershipNameController.show())
        mockAuthAdminRole()

        mockGetCompanyNameSuccess(testCompanyNumber, LimitedPartnership)

        val request = testPostRequest(testCompanyNumber)

        val result = TestCaptureCompanyNumberController.submit(request)
        status(result) shouldBe Status.NOT_IMPLEMENTED
      }
    }

    "form unsuccessfully submitted" should {
      "reload the page with errors" in {
        mockAuthAdminRole()

        val result = TestCaptureCompanyNumberController.submit(testPostRequest("123456789"))
        status(result) shouldBe Status.BAD_REQUEST
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }



    "company number failed prefix validation" should {
      "throw an InternalServerException" in {
        mockAuthAdminRole()
        // Redirect to error page

        val testCrn = "ZZ12345"
        val request = testPostRequest(testCrn)

        val result = TestCaptureCompanyNumberController.submit(request)
        intercept[InternalServerException](await(TestCaptureCompanyNumberController.submit(request)))

      }
    }


    "company number failed validation - invalid format" should {
      "throw an InternalServerException" in {
        mockAuthAdminRole()
        // Redirect to error page

        val testCrn = "123A456 A"
        val request = testPostRequest(testCrn)

        val result = TestCaptureCompanyNumberController.submit(request)
        intercept[InternalServerException](await(TestCaptureCompanyNumberController.submit(request)))

      }
    }

    "company number failed validation - zero is invalid" should {
      "throw an InternalServerException" in {
        mockAuthAdminRole()
        // Redirect to error page

        val testCrn = "0"
        val request = testPostRequest(testCrn)

        val result = TestCaptureCompanyNumberController.submit(request)
        intercept[InternalServerException](await(TestCaptureCompanyNumberController.submit(request)))

      }
    }

    "get company name returned not found" should {
      "throw an InternalServerException" in {
        mockAuthAdminRole()
        // Redirect to error page

        mockGetCompanyNameNotFound(testCompanyNumber)

        val request = testPostRequest(testCompanyNumber)

        val result = TestCaptureCompanyNumberController.submit(request)
        intercept[InternalServerException](await(TestCaptureCompanyNumberController.submit(request)))
      }
    }

    "get company name fails" should {
      "throw an InternalServerException" in {
        mockAuthAdminRole()

        mockGetCompanyNameFailure(testCompanyNumber)

        val request = testPostRequest(testCompanyNumber)

        intercept[InternalServerException](await(TestCaptureCompanyNumberController.submit(request)))
      }
    }
  }

}
