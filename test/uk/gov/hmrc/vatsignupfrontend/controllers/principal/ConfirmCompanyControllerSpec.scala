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

import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStoreCompanyNumberService

class ConfirmCompanyControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents
  with MockStoreCompanyNumberService {

  object TestConfirmCompanyController extends ConfirmCompanyController(
    mockControllerComponents,
    mockStoreCompanyNumberService
  )

  val testGetRequest = FakeRequest("GET", "/confirm-company")

  val testPostRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/confirm-company")

  "Calling the show action of the Confirm Company controller" when {
    "there is a company name in the session" should {
      "go to the Confirm Company page" in {
        mockAuthAdminRole()
        val request = testGetRequest.withSession(
          SessionKeys.companyNameKey -> testCompanyName
        )

        val result = TestConfirmCompanyController.show(request)
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")

        val changeLink = Jsoup.parse(contentAsString(result)).getElementById("changeLink")
        changeLink.attr("href") shouldBe routes.CaptureCompanyNumberController.show().url
      }
    }

    "there isn't a company name in the session" should {
      "go to the capture company number page" in {
        mockAuthAdminRole()

        val result = TestConfirmCompanyController.show(testGetRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureCompanyNumberController.show().url)
      }
    }
  }

  "Calling the submit action of the Confirm Company controller" should {
    "go to the 'agree to receive emails' page if CT enrolled" in {
      mockAuthRetrieveIRCTEnrolment()
      mockStoreCompanyNumberSuccess(
        vatNumber = testVatNumber,
        companyNumber = testCompanyNumber,
        companyUtr = Some(testSaUtr)
      )

      val request = testPostRequest.withSession(
        SessionKeys.vatNumberKey -> testVatNumber,
        SessionKeys.companyNumberKey -> testCompanyNumber
      )

      val result = TestConfirmCompanyController.submit(request)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.AgreeCaptureEmailController.show().url)
    }
    "go to the 'capture company UTR' page if not CT enrolled" in {
      mockAuthRetrieveVatDecEnrolment()

      val request = testPostRequest.withSession(
        SessionKeys.vatNumberKey -> testVatNumber,
        SessionKeys.companyNumberKey -> testCompanyNumber
      )

      val result = TestConfirmCompanyController.submit(request)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.CaptureCompanyUtrController.show().url)
    }

    "throw internal server exception if store company number fails" in {
      mockAuthRetrieveIRCTEnrolment()
      mockStoreCompanyNumberFailure(testVatNumber, testCompanyNumber, Some(testSaUtr))

      val request = testPostRequest.withSession(
        SessionKeys.vatNumberKey -> testVatNumber,
        SessionKeys.companyNumberKey -> testCompanyNumber
      )

      intercept[InternalServerException] {
        await(TestConfirmCompanyController.submit(request))
      }
    }
    "go to the 'your vat number' page if vat number is missing" in {
      mockAuthRetrieveIRCTEnrolment()

      val request = testPostRequest.withSession(
        SessionKeys.companyNumberKey -> testCompanyNumber
      )

      val result = TestConfirmCompanyController.submit(request)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ResolveVatNumberController.resolve().url)

    }
    "go to the 'capture company number' page if company number is missing" in {
      mockAuthRetrieveIRCTEnrolment()

      val request = testPostRequest.withSession(
        SessionKeys.vatNumberKey -> testVatNumber
      )

      val result = TestConfirmCompanyController.submit(request)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.CaptureCompanyNumberController.show().url)
    }
  }

}
