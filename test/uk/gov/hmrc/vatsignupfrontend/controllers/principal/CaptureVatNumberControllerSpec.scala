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
import uk.gov.hmrc.auth.core.retrieve.{Retrievals, ~}
import uk.gov.hmrc.auth.core.{Admin, Enrolments}
import uk.gov.hmrc.http.{InternalServerException, NotFoundException}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{FeatureSwitching, KnownFactsJourney}
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.forms.VatNumberForm
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockVatNumberEligibilityService

import scala.concurrent.Future


class CaptureVatNumberControllerSpec extends UnitSpec with GuiceOneAppPerSuite
  with MockControllerComponents with MockVatNumberEligibilityService with FeatureSwitching {

  object TestCaptureVatNumberController extends CaptureVatNumberController(mockControllerComponents, mockVatNumberEligibilityService)

  lazy val testGetRequest = FakeRequest("GET", "/vat-number")

  def testPostRequest(vatNumber: String): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/vat-number").withFormUrlEncodedBody(VatNumberForm.vatNumber -> vatNumber)

  "Calling the show action of the Capture Vat Number controller" when {
    "the known facts journey feature switch is enabled and the user does not have VAT-DEC enrolment" should {
      "go to the Capture Vat Number page" in {
        enable(KnownFactsJourney)
        mockAuthorise(
          retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
        )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))


        val result = TestCaptureVatNumberController.show(testGetRequest)

        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
    "the user has a VAT-DEC enrolment" should {
      "redirect to resolve VAT number controller" in {
        enable(KnownFactsJourney)
        mockAuthRetrieveVatDecEnrolment()

        val result = TestCaptureVatNumberController.show(testGetRequest)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.ResolveVatNumberController.resolve().url)
      }
    }

    "the known facts journey feature switch is disabled" should {
      "throw not found exception" in {
        mockAuthorise(
          retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
        )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))

        intercept[NotFoundException] {
          await(TestCaptureVatNumberController.show(testGetRequest))
        }
      }
    }
  }

  "Calling the submit action of the Capture Vat Number controller" when {
    "form successfully submitted" when {
      "the vat number passes checksum validation" should {
        "the user has a VAT-DEC enrolment" should {
          "redirect to resolve VAT number controller" in {
            enable(KnownFactsJourney)
            mockAuthRetrieveVatDecEnrolment()

            val result = TestCaptureVatNumberController.submit(testPostRequest(testVatNumber))

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(routes.ResolveVatNumberController.resolve().url)
          }
        }

        "the known facts journey feature switch is disabled" should {
          "throw not found exception" in {
            mockAuthorise(
              retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
            )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))

            intercept[NotFoundException] {
              await(TestCaptureVatNumberController.submit(testPostRequest(testVatNumber)))
            }
          }
        }
        "redirect to the Capture Vat Registration Date page when the vat number is eligible" in {
          enable(KnownFactsJourney)
          mockAuthorise(
            retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
          )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))
          mockVatNumberEligibilitySuccess(testVatNumber)

          implicit val request = testPostRequest(testVatNumber)

          val result = TestCaptureVatNumberController.submit(request)
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CaptureVatRegistrationDateController.show().url)

          result.session get SessionKeys.vatNumberKey should contain(testVatNumber)
        }

        "redirect to Cannot use service yet when the vat number is ineligible for Making Tax Digital" in {
          enable(KnownFactsJourney)
          mockAuthorise(
            retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
          )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))
          mockVatNumberIneligibleForMtd(testVatNumber)

          val request = testPostRequest(testVatNumber)

          val result = TestCaptureVatNumberController.submit(request)
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CannotUseServiceController.show().url)
        }

        "redirect to Invalid Vat Number page when the vat number is invalid" in {
          enable(KnownFactsJourney)
          mockAuthorise(
            retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
          )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))
          mockVatNumberEligibilityInvalid(testVatNumber)

          val request = testPostRequest(testVatNumber)

          val result = TestCaptureVatNumberController.submit(request)
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvalidVatNumberController.show().url)
        }

        "redirect to Already Signed Up page when the vat number has already been subscribed" in {
          enable(KnownFactsJourney)
          mockAuthorise(
            retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
          )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))
          mockVatNumberEligibilityAlreadySubscribed(testVatNumber)

          val request = testPostRequest(testVatNumber)

          val result = TestCaptureVatNumberController.submit(request)
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.AlreadySignedUpController.show().url)
        }

        "throw an exception for any other scenario" in {
          enable(KnownFactsJourney)
          mockAuthorise(
            retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
          )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))
          mockVatNumberEligibilityFailure(testVatNumber)

          val request = testPostRequest(testVatNumber)
          intercept[InternalServerException] {
            await(TestCaptureVatNumberController.submit(request))
          }
        }
      }
      "the vat number fails checksum validation" should {
        "redirect to Invalid Vat Number page" in {
          enable(KnownFactsJourney)
          mockAuthorise(
            retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
          )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))
          mockVatNumberEligibilitySuccess(testInvalidVatNumber)

          implicit val request = testPostRequest(testInvalidVatNumber)

          val result = TestCaptureVatNumberController.submit(request)
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvalidVatNumberController.show().url)
        }
      }
    }

    "form unsuccessfully submitted" should {
      "reload the page with errors" in {
        enable(KnownFactsJourney)
        mockAuthorise(
          retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
        )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))

        val result = TestCaptureVatNumberController.submit(testPostRequest("invalid"))
        status(result) shouldBe Status.BAD_REQUEST
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }

}
