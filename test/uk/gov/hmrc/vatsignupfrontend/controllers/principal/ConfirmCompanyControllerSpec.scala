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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.CompanyNameJourney
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStoreCompanyNumberService

class ConfirmCompanyControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents
  with MockStoreCompanyNumberService {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(CompanyNameJourney)
  }

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
      "go to the capture company name page" in {
        mockAuthAdminRole()

        val result = TestConfirmCompanyController.show(testGetRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureCompanyNumberController.show().url)
      }
    }
  }

  "Calling the submit action of the Confirm Company controller" should {
    "go to the 'agree to receive emails' page" in {
      mockAuthAdminRole()
      mockStoreCompanyNumberSuccess(testVatNumber, testCompanyNumber)

      val request = testPostRequest.withSession(
        SessionKeys.vatNumberKey -> testVatNumber,
        SessionKeys.companyNumberKey -> testCompanyNumber
      )

      val result = TestConfirmCompanyController.submit(request)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.AgreeCaptureEmailController.show().url)
    }
    // todo if store fails
    // todo if session variables are missing
  }

}
