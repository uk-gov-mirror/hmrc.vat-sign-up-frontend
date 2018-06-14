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
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.{Admin, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.{EmptyRetrieval, Retrievals, ~}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.KnownFactsJourney
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStoreVatNumberService

import scala.concurrent.Future

class YourVatNumberControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents
  with MockStoreVatNumberService {

  object TestYourVatNumberController extends YourVatNumberController(mockControllerComponents, mockStoreVatNumberService)

  lazy val testGetRequest = FakeRequest("GET", "/confirm-vat-number")

  lazy implicit val testPostRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/confirm-vat-number")

  "Calling the show action of the Confirm Vat Number controller" when {
    "the user has a VAT-DEC enrolment" should {
      "go to the Confirm Vat number page" in {
        mockAuthRetrieveVatDecEnrolment()
        val request = testGetRequest

        val result = TestYourVatNumberController.show(request)
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }

  "the user does not have a VAT-DEC enrolment" when {
    "the known facts journey feature switch is enabled" should {
      "redirect to resolve VAT number controller" in {
        enable(KnownFactsJourney)

        mockAuthorise(
          retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
        )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))

        val result = TestYourVatNumberController.show(testGetRequest)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.ResolveVatNumberController.resolve().url)
      }
    }

    "the known facts journey feature switch is disabled" should {
      "redirect to resolve VAT number controller" in {
        mockAuthorise(
          retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
        )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))

        val result = TestYourVatNumberController.show(testGetRequest)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.ResolveVatNumberController.resolve().url)
      }
    }
  }

  "Calling the submit action of the Your Vat Number controller" when {
    "the user has a VAT-DEC enrolment" when {
      "store vat is successful" should {
        "return the capture business entity page with the vat number in session" in {
          mockAuthRetrieveVatDecEnrolment()
          mockStoreVatNumberSuccess(vatNumber = testVatNumber)

          val result = TestYourVatNumberController.submit(testPostRequest)
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.CaptureBusinessEntityController.show().url)
          result.session.get(SessionKeys.vatNumberKey) should contain(testVatNumber)
        }
      }

      "vat number is already subscribed" should {
        "redirect to the already subscribed page" in {
          mockAuthRetrieveVatDecEnrolment()
          mockStoreVatNumberAlreadySubscribed(vatNumber = testVatNumber)

          val result = TestYourVatNumberController.submit(testPostRequest)
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.AlreadySignedUpController.show().url)
        }
      }
      "the vat number is ineligible" should {
        "redirect to the already subscribed page" in {
          mockAuthRetrieveVatDecEnrolment()
          mockStoreVatNumberIneligible(vatNumber = testVatNumber)

          val result = TestYourVatNumberController.submit(testPostRequest)
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CannotUseServiceController.show().url)
        }
      }

      "store vat returns any other error" should {
        "throw internal server exception" in {
          mockAuthRetrieveVatDecEnrolment()
          mockStoreVatNumberFailure(vatNumber = testVatNumber)

          intercept[InternalServerException] {
            await(TestYourVatNumberController.submit(testPostRequest))
          }
        }
      }
    }
    "the user does not have a VAT-DEC enrolment" when {
      "the known facts journey feature switch is enabled" should {
        "redirect to resolve VAT number controller" in {
          enable(KnownFactsJourney)

          mockAuthorise(
            retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
          )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))

          val result = TestYourVatNumberController.submit(testPostRequest)

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.ResolveVatNumberController.resolve().url)
        }
      }

      "the known facts journey feature switch is disabled" should {
        "redirect to resolve VAT number controller" in {
          mockAuthorise(
            retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
          )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))

          val result = TestYourVatNumberController.submit(testPostRequest)

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.ResolveVatNumberController.resolve().url)
        }
      }
    }
  }

}
