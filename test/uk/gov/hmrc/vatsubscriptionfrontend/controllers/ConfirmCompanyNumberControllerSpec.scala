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
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys
import uk.gov.hmrc.vatsubscriptionfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsubscriptionfrontend.services.mocks.MockStoreCompanyNumberService

import scala.concurrent.Future

class ConfirmCompanyNumberControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents
  with MockStoreCompanyNumberService {

  object TestConfirmCompanyNumberController extends ConfirmCompanyNumberController(mockControllerComponents, mockStoreCompanyNumberService)

  val testGetRequest = FakeRequest("GET", "/confirm-company-number")

  val testPostRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/confirm-company-number")

  "Calling the show action of the Confirm Company Number controller" when {
    "there is a company number in the session" should {
      "go to the Confirm Company Number page" in {
        mockAuthorise(retrievals = EmptyRetrieval)(Future.successful(Unit))
        val request = testGetRequest.withSession(SessionKeys.companyNumberKey -> testCompanyNumber)

        val result = TestConfirmCompanyNumberController.show(request)
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "there isn't a company number in the session" should {
      "go to the Confirm company number page" in {
        mockAuthorise(retrievals = EmptyRetrieval)(Future.successful(Unit))

        val result = TestConfirmCompanyNumberController.show(testGetRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureCompanyNumberController.show().url)
      }
    }
  }


  "Calling the submit action of the Confirm Company Number controller" when {
    "vat number is in session and store vat is successful" should {
      // todo goto email
      "return not implemented" in {
        mockAuthorise(retrievals = EmptyRetrieval)(Future.successful(Unit))
        mockStoreCompanyNumberSuccess(vatNumber = testCompanyNumber)

        val result = TestConfirmCompanyNumberController.submit(testPostRequest.withSession(SessionKeys.companyNumberKey -> testCompanyNumber))
        status(result) shouldBe Status.NOT_IMPLEMENTED
      }
    }
    "vat number is in session but store vat is unsuccessful" should {
      "throw internal server exception" in {
        mockAuthorise(retrievals = EmptyRetrieval)(Future.successful(Unit))
        mockStoreCompanyNumberFailure(vatNumber = testCompanyNumber)

        intercept[InternalServerException] {
          await(TestConfirmCompanyNumberController.submit(testPostRequest.withSession(SessionKeys.companyNumberKey -> testCompanyNumber)))
        }
      }
    }
    "vat number is not in session" should {
      "redirect to capture vat number" in {
        mockAuthorise(retrievals = EmptyRetrieval)(Future.successful(Unit))

        val result = TestConfirmCompanyNumberController.submit(testPostRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureCompanyNumberController.show().url)
      }
    }
  }
}