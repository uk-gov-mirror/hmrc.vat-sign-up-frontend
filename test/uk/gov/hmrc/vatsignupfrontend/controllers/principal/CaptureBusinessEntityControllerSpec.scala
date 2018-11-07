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
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.{Retrievals, ~}
import uk.gov.hmrc.auth.core.{Admin, Enrolments}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{CtKnownFactsIdentityVerification, UseIRSA}
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.forms.BusinessEntityForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockCitizenDetailsService

import scala.concurrent.Future

class CaptureBusinessEntityControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents
  with MockCitizenDetailsService {


  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(UseIRSA)
    disable(CtKnownFactsIdentityVerification)
  }

  object TestCaptureBusinessEntityController extends CaptureBusinessEntityController(mockControllerComponents, mockCitizenDetailsService)

  val testGetRequest = FakeRequest("GET", "/business-type")

  def testPostRequest(entityTypeVal: String): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/business-type").withFormUrlEncodedBody(businessEntity -> entityTypeVal)

  "Calling the show action of the Capture Entity Type controller" should {
    "go to the Capture Entity Type page" in {
      mockAuthAdminRole()

      val result = TestCaptureBusinessEntityController.show(testGetRequest)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "Calling the submit action of the Capture Business Entity controller" when {
    "form successfully submitted" when {

      "the business entity is sole trader" when {
        "go to capture your details with sole trader stored in session" in {
          mockAuthRetrieveVatDecEnrolment()

          implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(soleTrader)

          val result = await(TestCaptureBusinessEntityController.submit(request))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.CaptureYourDetailsController.show().url)

          result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(SoleTrader))
        }
      }
      "the user has IRSA enrolment" when {
        "calls to CID is successful" should {
          "go to Confirm your retrieved details with sole trader and user details stored in session" in {
            mockAuthRetrieveVatDecEnrolment(hasIRSAEnrolment = true)
            mockCitizenDetailsSuccess(testSaUtr, testUserDetails)

            implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(soleTrader)

            val result = await(TestCaptureBusinessEntityController.submit(request))
            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(routes.ConfirmYourRetrievedUserDetailsController.show().url)

            result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(SoleTrader))
            result.session get SessionKeys.userDetailsKey should contain(Json.toJson(testUserDetails).toString)
          }
        }
        "CID returns NoCitizenRecord" should {
          "throw Internal server exception" in {
            mockAuthRetrieveVatDecEnrolment(hasIRSAEnrolment = true)
            mockCitizenDetailsFailure(testSaUtr)

            implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(soleTrader)

            intercept[InternalServerException] {
              await(TestCaptureBusinessEntityController.submit(request))
            }
          }
        }
        "CID returns MoreThanOneCitizenMatched" should {
          "throw Internal server exception" in {
            mockAuthRetrieveVatDecEnrolment(hasIRSAEnrolment = true)
            mockCitizenDetailsFailure(testSaUtr)

            implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(soleTrader)

            intercept[InternalServerException] {
              await(TestCaptureBusinessEntityController.submit(request))
            }
          }
        }
        "calls to CID fails" should {
          "throw Internal server exception" in {
            mockAuthRetrieveVatDecEnrolment(hasIRSAEnrolment = true)
            mockCitizenDetailsFailure(testSaUtr)

            implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(soleTrader)

            intercept[InternalServerException] {
              await(TestCaptureBusinessEntityController.submit(request))
            }
          }
        }
      }
      "the user does not have IRSA enrolment" when {
        "go to capture your details with sole trader stored in session" in {
          mockAuthRetrieveVatDecEnrolment()

          implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(soleTrader)

          val result = await(TestCaptureBusinessEntityController.submit(request))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.CaptureYourDetailsController.show().url)

          result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(SoleTrader))
        }
      }

      "the business entity is limited company" when {
        "there is a VATDEC enrolment" should {
          "go to capture company number controller" in {
            mockAuthRetrieveVatDecEnrolment()
            implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(limitedCompany)

            val result = await(TestCaptureBusinessEntityController.submit(request))
            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(routes.CaptureCompanyNumberController.show().url)

            result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(LimitedCompany))
          }
        }
        "there is not a VATDEC enrolment and" when {
          "CtKnownFactsIdentityVerification is disabled" should {
            "go to capture your details controller" in {
              mockAuthorise(
                retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
              )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))
              implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(limitedCompany)

              val result = await(TestCaptureBusinessEntityController.submit(request))
              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) should contain(routes.CaptureYourDetailsController.show().url)

              result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(LimitedCompany))
            }
          }
          "CtKnownFactsIdentityVerification is enabled" should {
            "go to capture company number controller" in {
              enable(CtKnownFactsIdentityVerification)
              mockAuthorise(
                retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
              )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))
              implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(limitedCompany)

              val result = await(TestCaptureBusinessEntityController.submit(request))
              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) should contain(routes.CaptureCompanyNumberController.show().url)

              result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(LimitedCompany))
            }
          }
        }
      }

      "the business entity is general partnership" when {
        "go to resolve partnership utr controller" in {
          mockAuthRetrieveVatDecEnrolment()
          implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(generalPartnership)

          val result = await(TestCaptureBusinessEntityController.submit(request))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(partnerships.routes.ResolvePartnershipUtrController.resolve().url)

          result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(GeneralPartnership))
        }
      }

      "the business entity is limited partnership" when {
        "there is a VATDEC enrolment" should {
          "go to capture partnership company number controller" in {
            mockAuthRetrieveVatDecEnrolment()
            implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(limitedPartnership)

            val result = await(TestCaptureBusinessEntityController.submit(request))
            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(partnerships.routes.CapturePartnershipCompanyNumberController.show().url)

            result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(LimitedPartnership))
          }
        }
      }

      "the business entity is vat group" when {
        "goto vat group resolver" in {
          mockAuthRetrieveVatDecEnrolment()
          implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(vatGroup)

          val result = await(TestCaptureBusinessEntityController.submit(request))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.VatGroupResolverController.resolve().url)

          result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(VatGroup))
        }
      }

      "the business entity is other" should {
        "go to Cannot use service yet page" in {
          mockAuthorise(
            retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
          )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))

          implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(other)

          val result = await(TestCaptureBusinessEntityController.submit(request))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.CannotUseServiceController.show().url)

          result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(Other))
        }
      }
    }

    "form unsuccessfully submitted" should {
      "reload the page with errors" in {
        mockAuthorise(
          retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
        )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))


        val result = TestCaptureBusinessEntityController.submit(testPostRequest("invalid"))
        status(result) shouldBe Status.BAD_REQUEST
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

  }

}