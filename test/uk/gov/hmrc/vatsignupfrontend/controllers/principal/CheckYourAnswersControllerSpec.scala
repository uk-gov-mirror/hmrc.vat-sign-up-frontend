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

import java.time.LocalDate

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{CtKnownFactsIdentityVerification, KnownFactsJourney}
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStoreVatNumberService

class CheckYourAnswersControllerSpec extends UnitSpec with GuiceOneAppPerSuite
  with MockControllerComponents
  with MockStoreVatNumberService {

  object TestCheckYourAnswersController extends CheckYourAnswersController(mockControllerComponents, mockStoreVatNumberService)

  val testDate: DateModel = DateModel.dateConvert(LocalDate.now())

  def testGetRequest(vatNumber: Option[String] = Some(testVatNumber),
                     registrationDate: Option[DateModel] = Some(testDate),
                     postCode: Option[PostCode] = Some(testBusinessPostcode),
                     businessType: Option[BusinessEntity] = Some(SoleTrader)
                    ): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("GET", "/check-your-answers").withSession(
      SessionKeys.vatNumberKey -> vatNumber.getOrElse(""),
      SessionKeys.vatRegistrationDateKey -> registrationDate.map(Json.toJson(_).toString()).getOrElse(""),
      SessionKeys.businessPostCodeKey -> postCode.map(Json.toJson(_).toString()).getOrElse(""),
      SessionKeys.businessEntityKey -> businessType.map(BusinessEntitySessionFormatter.toString).getOrElse("")
    )

  def testPostRequest(vatNumber: Option[String] = Some(testVatNumber),
                      registrationDate: Option[DateModel] = Some(testDate),
                      postCode: Option[PostCode] = Some(testBusinessPostcode),
                      businessType: Option[BusinessEntity] = Some(SoleTrader)
                     ): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/check-your-answers").withSession(
      SessionKeys.vatNumberKey -> vatNumber.getOrElse(""),
      SessionKeys.vatRegistrationDateKey -> registrationDate.map(Json.toJson(_).toString()).getOrElse(""),
      SessionKeys.businessPostCodeKey -> postCode.map(Json.toJson(_).toString()).getOrElse(""),
      SessionKeys.businessEntityKey -> businessType.map(BusinessEntitySessionFormatter.toString).getOrElse("")
    )

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(KnownFactsJourney)
    disable(CtKnownFactsIdentityVerification)
  }

  "Calling the show action of the Check your answers controller" when {
    "all prerequisite data are in session" should {
      "go to the Check your answers page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.show(testGetRequest())
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
    "vat number is missing" should {
      "go to capture vat number page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.show(testGetRequest(vatNumber = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureVatNumberController.show().url)
      }
    }
    "vat registration date is missing" should {
      "go to capture vat registration date page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.show(testGetRequest(registrationDate = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureVatRegistrationDateController.show().url)
      }
    }
    "post code is missing" should {
      "go to business post code page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.show(testGetRequest(postCode = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.BusinessPostCodeController.show().url)
      }
    }
    "business entity is missing" should {
      "go to business entity page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.show(testGetRequest(businessType = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureBusinessEntityController.show().url)
      }
    }
    "business entity is Other" should {
      "go to business entity page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.show(testGetRequest(businessType = Some(Other)))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureBusinessEntityController.show().url)
      }
    }
  }

  "Calling the submit action of the Check your answers controller" when {
    "all prerequisite data are in" when {
      "store vat number returned StoreVatNumberSuccess" when {
        "businessType is sole trader" when {
          "CtKnownFactsIdentityVerification is disabled" should {
            "goto capture your details controller" in {
              mockAuthAdminRole()
              mockStoreVatNumberSuccess(testVatNumber, testBusinessPostcode, testDate)

              val result = await(TestCheckYourAnswersController.submit(testPostRequest()))
              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) should contain(routes.CaptureYourDetailsController.show().url)
            }
          }
          "CtKnownFactsIdentityVerification is enabled" should {
            "goto capture your details controller" in {
              enable(CtKnownFactsIdentityVerification)

              mockAuthAdminRole()
              mockStoreVatNumberSuccess(testVatNumber, testBusinessPostcode, testDate)

              val result = await(TestCheckYourAnswersController.submit(testPostRequest()))
              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) should contain(routes.CaptureYourDetailsController.show().url)
            }
          }
        }
        "businessType is limited company" when {
          "CtKnownFactsIdentityVerification is disabled" should {
            "goto capture your details controller" in {
              mockAuthAdminRole()
              mockStoreVatNumberSuccess(testVatNumber, testBusinessPostcode, testDate)

              val result = await(TestCheckYourAnswersController.submit(testPostRequest(businessType = Some(LimitedCompany))))
              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) should contain(routes.CaptureYourDetailsController.show().url)
            }
          }
          "CtKnownFactsIdentityVerification is enabled" should {
            "goto capture company number controller" in {
              enable(CtKnownFactsIdentityVerification)

              mockAuthAdminRole()
              mockStoreVatNumberSuccess(testVatNumber, testBusinessPostcode, testDate)

              val result = await(TestCheckYourAnswersController.submit(testPostRequest(businessType = Some(LimitedCompany))))
              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) should contain(routes.CaptureCompanyNumberController.show().url)
            }
          }
        }
      }
      "store vat number returned KnownFactsMismatch" should {
        "go to the could not confirm business page" in {
          mockAuthAdminRole()
          mockStoreVatNumberKnownFactsMismatch(testVatNumber, testBusinessPostcode, testDate)

          val result = TestCheckYourAnswersController.submit(testGetRequest())
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CouldNotConfirmBusinessController.show().url)
        }
      }
      "store vat number returned InvalidVatNumber" should {
        "go to the invalid vat number page" in {
          mockAuthAdminRole()
          mockStoreVatNumberInvalid(testVatNumber, testBusinessPostcode, testDate)

          val result = TestCheckYourAnswersController.submit(testGetRequest())
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvalidVatNumberController.show().url)
        }
      }
      "store vat number returned IneligibleVatNumber" should {
        "go to the could not use service page" in {
          mockAuthAdminRole()
          mockStoreVatNumberIneligible(testVatNumber, testBusinessPostcode, testDate)

          val result = TestCheckYourAnswersController.submit(testGetRequest())
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CannotUseServiceController.show().url)
        }
      }
      "store vat number returned AlreadySubscribed" should {
        "go to the already signed up page" in {
          mockAuthAdminRole()
          mockStoreVatNumberAlreadySubscribed(testVatNumber, testBusinessPostcode, testDate)

          val result = TestCheckYourAnswersController.submit(testGetRequest())
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.AlreadySignedUpController.show().url)
        }
      }
      "store vat number returned a failure" should {
        "throw internal server exception" in {
          mockAuthAdminRole()
          mockStoreVatNumberFailure(testVatNumber, testBusinessPostcode, testDate)

          intercept[InternalServerException] {
            await(TestCheckYourAnswersController.submit(testGetRequest()))
          }

        }
      }
    }
    "vat number is missing" should {
      "go to capture vat number page" in {
        mockAuthAdminRole()

        val result = await(TestCheckYourAnswersController.submit(testPostRequest(vatNumber = None)))

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureVatNumberController.show().url)
      }
    }
    "vat registration date is missing" should {
      "go to capture vat registration date page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.submit(testPostRequest(registrationDate = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureVatRegistrationDateController.show().url)
      }
    }
    "post code is missing" should {
      "go to business post code page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.submit(testPostRequest(postCode = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.BusinessPostCodeController.show().url)
      }
    }
    "business entity is missing" should {
      "go to business entity page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.submit(testPostRequest(businessType = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureBusinessEntityController.show().url)
      }
    }
    "business entity is Other" should {
      "go to business entity page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.submit(testPostRequest(businessType = Some(Other)))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureBusinessEntityController.show().url)
      }
    }
  }

}