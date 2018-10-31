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
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.{Retrievals, ~}
import uk.gov.hmrc.auth.core.{Admin, Enrolments}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.GeneralPartnershipJourney
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StorePartnershipInformationHttpParser.{StorePartnershipInformationFailureResponse, StorePartnershipInformationSuccess}
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStorePartnershipInformationService

import scala.concurrent.Future

class CheckYourAnswersPartnershipsControllerSpec extends UnitSpec with GuiceOneAppPerSuite
  with MockControllerComponents
  with MockStorePartnershipInformationService {

  object TestCheckYourAnswersController extends CheckYourAnswersPartnershipsController(mockControllerComponents, mockStorePartnershipInformationService)

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(GeneralPartnershipJourney)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    disable(GeneralPartnershipJourney)
  }

  def testGetRequest(sautr: Option[String] = Some(testCompanyUtr),
                     partnershipEntityType: Option[String] = Some(testPartnershipType),
                     postCode: Option[PostCode] = Some(testBusinessPostcode)
                    ): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("GET", "/check-your-answers-partnership").withSession(
      SessionKeys.partnershipSautrKey -> sautr.getOrElse(""),
      SessionKeys.partnershipTypeKey -> partnershipEntityType.getOrElse(""),
      SessionKeys.partnershipPostCodeKey -> Json.toJson(postCode).toString
    )

  def testPostRequest(vatNumber: Option[String] = Some(testVatNumber),
                      sautr: Option[String] = Some(testCompanyUtr),
                      partnershipEntityType: Option[String] = Some(testPartnershipType),
                      postCode: Option[PostCode] = Some(testBusinessPostcode)
                     ): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/check-your-answers").withSession(
      SessionKeys.vatNumberKey -> vatNumber.getOrElse(""),
      SessionKeys.partnershipSautrKey -> sautr.getOrElse(""),
      SessionKeys.partnershipTypeKey -> partnershipEntityType.getOrElse(""),
      SessionKeys.partnershipPostCodeKey -> Json.toJson(postCode).toString
    )

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
    "partnership type is missing" should {
      "go to capture business entity page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.show(testGetRequest(partnershipEntityType = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(principalRoutes.CaptureBusinessEntityController.show().url)
      }
    }
    "partnership utr is missing" should {
      "go to capture partnership utr page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.show(testGetRequest(sautr = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CapturePartnershipUtrController.show().url)
      }
    }
    "post code is missing" should {
      "go to partnership post code page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.show(testGetRequest(postCode = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.PrincipalPlacePostCodeController.show().url)
      }
    }
  }

  "Calling the submit action of the Check your answers controller" when {
    "all prerequisite data are in" when {
      "store vat number returned VatNumberStored" should {
        "goto agree to receive email controller" in {
          mockAuthorise(
            retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
          )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))
          mockStorePartnershipInformation(
            vatNumber = testVatNumber,
            sautr = testCompanyUtr,
            companyNumber = None,
            partnershipEntity = Some(testPartnershipType),
            postCode = Some(testBusinessPostcode)
          )(Right(StorePartnershipInformationSuccess))

          val result = await(TestCheckYourAnswersController.submit(testPostRequest()))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(principalRoutes.AgreeCaptureEmailController.show().url)
        }
      }
      "store vat number returned a failure" should {
        "throw internal server exception" in {
          mockAuthorise(
            retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
          )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))
          mockStorePartnershipInformation(
            vatNumber = testVatNumber,
            sautr = testCompanyUtr,
            companyNumber = None,
            partnershipEntity = Some(testPartnershipType),
            postCode = Some(testBusinessPostcode)
          )(Left(StorePartnershipInformationFailureResponse(BAD_REQUEST)))

          intercept[InternalServerException] {
            await(TestCheckYourAnswersController.submit(testPostRequest()))
          }

        }
      }

    }
    "vat number is missing" should {
      "go to resolve vat number page" in {
        mockAuthorise(
          retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
        )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))

        val result = await(TestCheckYourAnswersController.submit(testPostRequest(vatNumber = None)))

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(principalRoutes.ResolveVatNumberController.resolve().url)
      }
    }
    "partnership utr is missing" should {
      "go to capture partnership utr page" in {
        mockAuthorise(
          retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
        )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))

        val result = TestCheckYourAnswersController.submit(testPostRequest(sautr = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CapturePartnershipUtrController.show().url)
      }
    }
    "partnership type is missing" should {
      "go to capture business entity page" in {
        mockAuthorise(
          retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
        )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))

        val result = TestCheckYourAnswersController.submit(testPostRequest(partnershipEntityType = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(principalRoutes.CaptureBusinessEntityController.show().url)
      }
    }
    "post code is missing" should {
      "go to partnership post code page" in {
        mockAuthorise(
          retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
        )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))

        val result = TestCheckYourAnswersController.submit(testPostRequest(postCode = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.PrincipalPlacePostCodeController.show().url)
      }
    }
  }

}
