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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.SessionKeys._
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{GeneralPartnershipJourney, LimitedPartnershipJourney}
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StorePartnershipInformationHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.models.PartnershipEntityType
import uk.gov.hmrc.vatsignupfrontend.models.PartnershipEntityType.CompanyTypeSessionFormatter
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStorePartnershipInformationService
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils.jsonSessionFormatter

class CheckYourAnswersPartnershipsControllerSpec extends UnitSpec with GuiceOneAppPerSuite
  with MockControllerComponents
  with MockStorePartnershipInformationService {

  object TestCheckYourAnswersController extends CheckYourAnswersPartnershipsController(mockControllerComponents, mockStorePartnershipInformationService)

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(GeneralPartnershipJourney)
    enable(LimitedPartnershipJourney)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    disable(GeneralPartnershipJourney)
    disable(LimitedPartnershipJourney)
  }

  val generalPartnershipType: String = PartnershipEntityType.GeneralPartnership.toString
  val limitedPartnershipType: String = PartnershipEntityType.LimitedPartnership.toString

  private def sessionValues(vatNumber: Option[String],
                            sautr: Option[String],
                            crn: Option[String],
                            postCode: Option[PostCode],
                            entityType: Option[PartnershipEntityType],
                            businessEntity: Option[BusinessEntity]): Iterable[(String, String)] =
    (
      (vatNumber map (vatNumberKey -> _))
        ++ (sautr map (partnershipSautrKey -> _))
        ++ (crn map (companyNumberKey -> _))
        ++ (postCode map jsonSessionFormatter[PostCode].toString map (partnershipPostCodeKey -> _))
        ++ (entityType map CompanyTypeSessionFormatter.toString map (partnershipTypeKey -> _))
        ++ (businessEntity map BusinessEntitySessionFormatter.toString map (businessEntityKey -> _))
      )


  def testGetRequest(vatNumber: Option[String] = Some(testVatNumber),
                     sautr: Option[String] = Some(testSaUtr),
                     crn: Option[String] = None,
                     postCode: Option[PostCode] = Some(testBusinessPostcode),
                     entityType: Option[PartnershipEntityType] = None,
                     businessEntity: Option[BusinessEntity] = None
                    ): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("GET", "/check-your-answers").withSession(
      sessionValues(vatNumber, sautr, crn, postCode, entityType, businessEntity).toSeq: _*
    )

  def testPostRequest(vatNumber: Option[String] = Some(testVatNumber),
                      sautr: Option[String] = Some(testSaUtr),
                      crn: Option[String] = None,
                      postCode: Option[PostCode] = Some(testBusinessPostcode),
                      entityType: Option[PartnershipEntityType] = None
                     ): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/check-your-answers").withSession(
      sessionValues(vatNumber, sautr, crn, postCode, entityType, None).toSeq: _*
    )

  "Calling the show action of the Check your answers controller" when {
    "all prerequisite data are in session" when {
      "the user is a general partnership and the GP feature switch is on" should {
        "go to the Check your answers page" in {
          mockAuthAdminRole()
          disable(LimitedPartnershipJourney)
          enable(GeneralPartnershipJourney)

          val result = TestCheckYourAnswersController.show(testGetRequest(
            businessEntity = Some(GeneralPartnership)
          ))

          status(result) shouldBe Status.OK
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
      "the user is a limited partnership and the LP feature switch is on" should {
        "go to the Check your answers page" in {
          mockAuthAdminRole()
          disable(GeneralPartnershipJourney)
          enable(LimitedPartnershipJourney)

          val result = TestCheckYourAnswersController.show(testGetRequest(
            entityType = Some(PartnershipEntityType.LimitedPartnership),
            businessEntity = Some(LimitedPartnership),
            crn = Some(testCompanyNumber)
          ))

          status(result) shouldBe Status.OK
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }
    "buisiness entity is missing" should {
      "go to capture business entity page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.show(testGetRequest(
          businessEntity = None
        ))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(principalRoutes.CaptureBusinessEntityController.show().url)
      }
    }
    "partnership type is LP and crn is missing" should {
      "go to capture partnership company number page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.show(testGetRequest(
          entityType = Some(PartnershipEntityType.LimitedPartnership),
          businessEntity = Some(LimitedPartnership),
          crn = None
        ))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CapturePartnershipCompanyNumberController.show().url)
      }
    }
    "partnership type is LP and partnership type is missing" should {
      "go to capture partnership company number page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.show(testGetRequest(
          businessEntity = Some(LimitedPartnership),
          crn = Some(testCompanyNumber)
        ))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CapturePartnershipCompanyNumberController.show().url)
      }
    }
    "partnership utr is missing for general partnership" should {
      "go to capture partnership utr page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.show(testGetRequest(
          businessEntity = Some(GeneralPartnership),
          sautr = None
        ))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CapturePartnershipUtrController.show().url)
      }
    }
    "partnership utr is missing for limited partnership" should {
      "go to capture partnership utr page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.show(testGetRequest(
          businessEntity = Some(GeneralPartnership),
          sautr = None,
          crn = Some(testCompanyNumber)
        ))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CapturePartnershipUtrController.show().url)
      }
    }
    "post code is missing" should {
      "go to partnership post code page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.show(testGetRequest(
          businessEntity = Some(GeneralPartnership),
          postCode = None
        ))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.PrincipalPlacePostCodeController.show().url)
      }
    }
  }

  "Calling the submit action of the Check your answers controller" when {
    "all prerequisite data are in" when {
      "store partnership information returned StorePartnershipInformationSuccess" when {
        "the user is a general partnership" should {
          "goto agree to receive email controller" in {
            enable(GeneralPartnershipJourney)
            disable(LimitedPartnershipJourney)

            mockAuthAdminRole()
            mockStorePartnershipInformation(
              vatNumber = testVatNumber,
              sautr = testSaUtr,
              postCode = Some(testBusinessPostcode)
            )(Right(StorePartnershipInformationSuccess))

            val result = await(TestCheckYourAnswersController.submit(testPostRequest()))
            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(principalRoutes.AgreeCaptureEmailController.show().url)
          }
        }
        "the user is a limited partnership" should {
          "goto agree to receive email controller" in {
            disable(GeneralPartnershipJourney)
            enable(LimitedPartnershipJourney)

            mockAuthAdminRole()
            mockStorePartnershipInformation(
              vatNumber = testVatNumber,
              sautr = testSaUtr,
              companyNumber = testCompanyNumber,
              partnershipEntity = PartnershipEntityType.LimitedPartnership,
              postCode = Some(testBusinessPostcode)
            )(Right(StorePartnershipInformationSuccess))

            val result = await(TestCheckYourAnswersController.submit(testPostRequest(
              entityType = Some(PartnershipEntityType.LimitedPartnership),
              crn = Some(testCompanyNumber)
            )))
            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(principalRoutes.AgreeCaptureEmailController.show().url)
          }
        }
      }
      "store partnership information returned a failure" should {
        "throw internal server exception" in {
          mockAuthAdminRole()
          mockStorePartnershipInformation(
            vatNumber = testVatNumber,
            sautr = testSaUtr,
            postCode = Some(testBusinessPostcode)
          )(Left(StorePartnershipInformationFailureResponse(BAD_REQUEST)))

          intercept[InternalServerException] {
            await(TestCheckYourAnswersController.submit(testPostRequest()))
          }
        }
      }
      " known facts mismatch failure on Store Partnership information" should {
        "redirect to known facts error page" in {
          mockAuthAdminRole()
          mockStorePartnershipInformation(
            vatNumber = testVatNumber,
            sautr = testSaUtr,
            companyNumber = testCompanyNumber,
            partnershipEntity = PartnershipEntityType.LimitedPartnership,
            postCode = Some(testBusinessPostcode)
          )(Left(StorePartnershipKnownFactsFailure))
          val result = await(TestCheckYourAnswersController.submit(testPostRequest(
            entityType = Some(PartnershipEntityType.LimitedPartnership),
            crn = Some(testCompanyNumber)
          )))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.CouldNotConfirmKnownFactsController.show().url)
        }
      }
    }
    "vat number is missing" should {
      "go to resolve vat number page" in {
        mockAuthAdminRole()

        val result = await(TestCheckYourAnswersController.submit(testPostRequest(vatNumber = None)))

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(principalRoutes.ResolveVatNumberController.resolve().url)
      }
    }
    "partnership type is LP and crn is missing" should {
      "go to capture partnership utr page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.submit(testPostRequest(
          entityType = Some(PartnershipEntityType.LimitedPartnership),
          crn = None
        ))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CapturePartnershipUtrController.show().url)
      }
    }
    "partnership type is LP and partnership type is missing" should {
      "go to capture partnership utr page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.submit(testPostRequest(
          entityType = None,
          crn = Some(testCompanyNumber)
        ))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CapturePartnershipUtrController.show().url)
      }
    }
    "partnership utr is missing" should {
      "go to capture partnership utr page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.submit(testPostRequest(sautr = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CapturePartnershipUtrController.show().url)
      }
    }
    "post code is missing" should {
      "go to capture partnership utr page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.submit(testPostRequest(
          postCode = None
        ))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CapturePartnershipUtrController.show().url)
      }
    }
  }

}
