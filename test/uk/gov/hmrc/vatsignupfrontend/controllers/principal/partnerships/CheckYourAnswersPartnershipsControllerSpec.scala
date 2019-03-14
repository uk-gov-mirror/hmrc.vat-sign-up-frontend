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
import uk.gov.hmrc.http.{InternalServerException, NotFoundException}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.SessionKeys._
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{GeneralPartnershipJourney, JointVenturePropertyJourney, LimitedPartnershipJourney}
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StorePartnershipInformationHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.models.PartnershipEntityType
import uk.gov.hmrc.vatsignupfrontend.models.PartnershipEntityType.CompanyTypeSessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models.YesNo.YesNoSessionFormatter
import uk.gov.hmrc.vatsignupfrontend.services.mocks.{MockStoreJointVentureInformationService, MockStorePartnershipInformationService}
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils.jsonSessionFormatter

import scala.concurrent.Future

class CheckYourAnswersPartnershipsControllerSpec extends UnitSpec with GuiceOneAppPerSuite
  with MockControllerComponents
  with MockStorePartnershipInformationService
  with MockStoreJointVentureInformationService {

  object TestCheckYourAnswersController extends CheckYourAnswersPartnershipsController(mockControllerComponents, mockStorePartnershipInformationService, mockStoreJointVentureInformationService)

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(GeneralPartnershipJourney)
    enable(LimitedPartnershipJourney)
  }

  val generalPartnershipType: String = PartnershipEntityType.GeneralPartnership.toString
  val limitedPartnershipType: String = PartnershipEntityType.LimitedPartnership.toString

  private def sessionValues(vatNumber: Option[String],
                            sautr: Option[String],
                            crn: Option[String],
                            postCode: Option[PostCode],
                            entityType: Option[PartnershipEntityType],
                            businessEntity: Option[BusinessEntity],
                            jointVentureProperty: Option[YesNo]): Iterable[(String, String)] =
    (
      (vatNumber map (vatNumberKey -> _))
        ++ (sautr map (partnershipSautrKey -> _))
        ++ (crn map (companyNumberKey -> _))
        ++ (postCode map jsonSessionFormatter[PostCode].toString map (partnershipPostCodeKey -> _))
        ++ (entityType map CompanyTypeSessionFormatter.toString map (partnershipTypeKey -> _))
        ++ (businessEntity map BusinessEntitySessionFormatter.toString map (businessEntityKey -> _))
        ++ (jointVentureProperty map YesNoSessionFormatter.toString map (jointVentureOrPropertyKey -> _))
      )


  def testGetRequest(vatNumber: Option[String] = Some(testVatNumber),
                     sautr: Option[String] = None,
                     crn: Option[String] = None,
                     postCode: Option[PostCode] = None,
                     entityType: Option[PartnershipEntityType] = None,
                     businessEntity: Option[BusinessEntity] = None,
                     jointVentureProperty: Option[YesNo] = None
                    ): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("GET", "/check-your-answers").withSession(
      sessionValues(vatNumber, sautr, crn, postCode, entityType, businessEntity, jointVentureProperty).toSeq: _*
    )

  def testPostRequest(vatNumber: Option[String] = Some(testVatNumber),
                      businessEntity: Option[BusinessEntity] = None,
                      sautr: Option[String] = None,
                      crn: Option[String] = None,
                      postCode: Option[PostCode] = None,
                      entityType: Option[PartnershipEntityType] = None
                     ): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/check-your-answers").withSession(
      sessionValues(vatNumber, sautr, crn, postCode, entityType, businessEntity, None).toSeq: _*
    )

  "Calling the show action of the Check your answers controller" when {

    "Partnership Entity is General Partnership" when {

      "the General Partnership feature is enabled" when {

        "the Joint Venture or Property Partnership feature is enabled" when {

          "The partnership is Joint Venture or Property" when {

            "All prerequisite data is in session" should {

              "Render the check your answers page" in {
                mockAuthAdminRole()
                enable(GeneralPartnershipJourney)
                enable(JointVenturePropertyJourney)

                val result = TestCheckYourAnswersController.show(testGetRequest(
                  businessEntity = Some(GeneralPartnership),
                  jointVentureProperty = Some(Yes)
                ))

                status(result) shouldBe Status.OK
                contentType(result) shouldBe Some("text/html")
                charset(result) shouldBe Some("utf-8")
              }
            }
          }

          "The partnership is NOT a Joint Venture or Property" when {

            "All prerequisite data is in session" should {

              "Render the check your answers page" in {
                mockAuthAdminRole()
                enable(GeneralPartnershipJourney)
                enable(JointVenturePropertyJourney)

                val result = TestCheckYourAnswersController.show(testGetRequest(
                  businessEntity = Some(GeneralPartnership),
                  jointVentureProperty = Some(No),
                  sautr = Some(testSaUtr),
                  postCode = Some(testBusinessPostcode)
                ))

                status(result) shouldBe Status.OK
                contentType(result) shouldBe Some("text/html")
                charset(result) shouldBe Some("utf-8")
              }
            }

            "SA UTR is missing from the session" should {

              "Redirect to Capture Pship SA UTR" in {
                mockAuthAdminRole()
                enable(GeneralPartnershipJourney)
                enable(JointVenturePropertyJourney)

                val result = TestCheckYourAnswersController.show(testGetRequest(
                  businessEntity = Some(GeneralPartnership),
                  jointVentureProperty = Some(No),
                  postCode = Some(testBusinessPostcode)
                ))

                status(result) shouldBe Status.SEE_OTHER
                redirectLocation(result) shouldBe Some(routes.CapturePartnershipUtrController.show().url)
              }
            }

            "Business Postcode is missing from the session" should {

              "Redirect to Capture Business Postcode" in {
                mockAuthAdminRole()
                enable(GeneralPartnershipJourney)
                enable(JointVenturePropertyJourney)

                val result = TestCheckYourAnswersController.show(testGetRequest(
                  businessEntity = Some(GeneralPartnership),
                  jointVentureProperty = Some(No),
                  sautr = Some(testSaUtr)
                ))

                status(result) shouldBe Status.SEE_OTHER
                redirectLocation(result) shouldBe Some(routes.PrincipalPlacePostCodeController.show().url)
              }
            }
          }

          "No answer has been provided for Joint Venture or Property" should {

            "Redirect to Joint Venture or Property page" in {
              mockAuthAdminRole()
              enable(GeneralPartnershipJourney)
              enable(JointVenturePropertyJourney)

              val result = TestCheckYourAnswersController.show(testGetRequest(
                businessEntity = Some(GeneralPartnership)
              ))

              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) shouldBe Some(routes.JointVentureOrPropertyController.show().url)
            }
          }
        }

        "the Joint Venture or Property Partnership feature is disabled" when {

          "All prerequisite data is in session" should {

            "Render the check your answers page" in {
              mockAuthAdminRole()
              enable(GeneralPartnershipJourney)
              disable(JointVenturePropertyJourney)

              val result = TestCheckYourAnswersController.show(testGetRequest(
                businessEntity = Some(GeneralPartnership),
                sautr = Some(testSaUtr),
                postCode = Some(testBusinessPostcode)
              ))

              status(result) shouldBe Status.OK
              contentType(result) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }
          }

          "SA UTR is missing from the session" should {

            "Redirect to Capture Pship SA UTR" in {
              mockAuthAdminRole()
              enable(GeneralPartnershipJourney)
              disable(JointVenturePropertyJourney)

              val result = TestCheckYourAnswersController.show(testGetRequest(
                businessEntity = Some(GeneralPartnership),
                postCode = Some(testBusinessPostcode)
              ))

              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) shouldBe Some(routes.CapturePartnershipUtrController.show().url)
            }
          }

          "Business Postcode is missing from the session" should {

            "Redirect to Capture Business Postcode" in {
              mockAuthAdminRole()
              enable(GeneralPartnershipJourney)
              disable(JointVenturePropertyJourney)

              val result = TestCheckYourAnswersController.show(testGetRequest(
                businessEntity = Some(GeneralPartnership),
                sautr = Some(testSaUtr)
              ))

              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) shouldBe Some(routes.PrincipalPlacePostCodeController.show().url)
            }
          }
        }
      }
    }

    "Partnership Entity is of type Limited Partnership" when {

      "the Limited Partnership feature is enabled" when {

        "All prerequisite data is in session" should {

          "Render the check your answers page" in {
            mockAuthAdminRole()
            enable(LimitedPartnershipJourney)

            val result = TestCheckYourAnswersController.show(testGetRequest(
              businessEntity = Some(LimitedPartnership),
              sautr = Some(testSaUtr),
              postCode = Some(testBusinessPostcode),
              crn = Some(testCompanyNumber),
              entityType = Some(PartnershipEntityType.LimitedLiabilityPartnership)
            ))

            status(result) shouldBe Status.OK
            contentType(result) shouldBe Some("text/html")
            charset(result) shouldBe Some("utf-8")
          }
        }

        "SA UTR is missing from the session" should {

          "redirect to the capture partnership SA UTR page" in {
            mockAuthAdminRole()
            enable(LimitedPartnershipJourney)

            val result = TestCheckYourAnswersController.show(testGetRequest(
              businessEntity = Some(LimitedPartnership),
              postCode = Some(testBusinessPostcode),
              crn = Some(testCompanyNumber),
              entityType = Some(PartnershipEntityType.LimitedLiabilityPartnership)
            ))

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.CapturePartnershipUtrController.show().url)
          }
        }

        "Business PostCode is missing from the session" should {

          "redirect to the capture partnership SA UTR page" in {
            mockAuthAdminRole()
            enable(LimitedPartnershipJourney)

            val result = TestCheckYourAnswersController.show(testGetRequest(
              businessEntity = Some(LimitedPartnership),
              sautr = Some(testSaUtr),
              crn = Some(testCompanyNumber),
              entityType = Some(PartnershipEntityType.LimitedLiabilityPartnership)
            ))

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.PrincipalPlacePostCodeController.show().url)
          }
        }

        "CRN is missing from the session" should {

          "redirect to the capture CRN page" in {
            mockAuthAdminRole()
            enable(LimitedPartnershipJourney)

            val result = TestCheckYourAnswersController.show(testGetRequest(
              businessEntity = Some(LimitedPartnership),
              sautr = Some(testSaUtr),
              postCode = Some(testBusinessPostcode),
              entityType = Some(PartnershipEntityType.LimitedLiabilityPartnership)
            ))

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.CapturePartnershipCompanyNumberController.show().url)
          }
        }

        "entity Type is missing from the session" should {

          "redirect to the capture CRN page" in {
            mockAuthAdminRole()
            enable(LimitedPartnershipJourney)

            val result = TestCheckYourAnswersController.show(testGetRequest(
              businessEntity = Some(LimitedPartnership),
              sautr = Some(testSaUtr),
              postCode = Some(testBusinessPostcode),
              crn = Some(testCompanyNumber)
            ))

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.CapturePartnershipCompanyNumberController.show().url)
          }
        }
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

            val result = await(TestCheckYourAnswersController.submit(testPostRequest(
              businessEntity = Some(GeneralPartnership),
              sautr = Some(testSaUtr),
              postCode = Some(testBusinessPostcode)
            )))
            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(principalRoutes.DirectDebitResolverController.show().url)
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
              businessEntity = Some(LimitedPartnership),
              entityType = Some(PartnershipEntityType.LimitedPartnership),
              crn = Some(testCompanyNumber),
              sautr = Some(testSaUtr),
              postCode = Some(testBusinessPostcode)
            )))
            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(principalRoutes.DirectDebitResolverController.show().url)
          }
        }
      }
      "store partnership information returned a failure" should {
        "throw internal server exception" in {
          mockAuthAdminRole()
          mockStorePartnershipInformation(
            vatNumber = testVatNumber,
            sautr = testSaUtr,
            companyNumber = testCompanyNumber,
            partnershipEntity = PartnershipEntityType.LimitedPartnership,
            postCode = Some(testBusinessPostcode)
          )(Left(StorePartnershipInformationFailureResponse(BAD_REQUEST)))

          intercept[InternalServerException] {
            await(TestCheckYourAnswersController.submit(testPostRequest(
              businessEntity = Some(LimitedPartnership),
              entityType = Some(PartnershipEntityType.LimitedPartnership),
              sautr = Some(testSaUtr),
              postCode = Some(testBusinessPostcode),
              crn = Some(testCompanyNumber)
            )))
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
            businessEntity = Some(LimitedPartnership),
            entityType = Some(PartnershipEntityType.LimitedPartnership),
            crn = Some(testCompanyNumber),
            sautr = Some(testSaUtr),
            postCode = Some(testBusinessPostcode)
          )))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.CouldNotConfirmKnownFactsController.show().url)
        }
      }
      "store partnership info returned NOT FOUND for SAUTR"  should {
        "goto could not confirm known facts page" in {
          enable(GeneralPartnershipJourney)
          disable(LimitedPartnershipJourney)

          mockAuthAdminRole()
          mockStorePartnershipInformation(
            vatNumber = testVatNumber,
            sautr = testSaUtr,
            companyNumber = testCompanyNumber,
            partnershipEntity = PartnershipEntityType.LimitedPartnership,
            postCode = Some(testBusinessPostcode)
          )(Left(PartnershipUtrNotFound))

          val result = await(TestCheckYourAnswersController.submit(testPostRequest(
            businessEntity = Some(LimitedPartnership),
            entityType = Some(PartnershipEntityType.LimitedPartnership),
            crn = Some(testCompanyNumber),
            sautr = Some(testSaUtr),
            postCode = Some(testBusinessPostcode)
          )))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.CouldNotConfirmKnownFactsController.show().url)
        }
      }
      " SAUTR NOT FOUND from ODS on Store Partnership information" should {
        "redirect to known facts error page" in {
          mockAuthAdminRole()
          mockStorePartnershipInformation(
            vatNumber = testVatNumber,
            sautr = testSaUtr,
            companyNumber = testCompanyNumber,
            partnershipEntity = PartnershipEntityType.LimitedPartnership,
            postCode = Some(testBusinessPostcode)
          )(Left(PartnershipUtrNotFound))
          val result = await(TestCheckYourAnswersController.submit(testPostRequest(
            businessEntity = Some(LimitedPartnership),
            entityType = Some(PartnershipEntityType.LimitedPartnership),
            crn = Some(testCompanyNumber),
            sautr = Some(testSaUtr),
            postCode = Some(testBusinessPostcode)
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
          businessEntity = Some(LimitedPartnership),
          entityType = Some(PartnershipEntityType.LimitedPartnership),
          crn = None,
          sautr = Some(testSaUtr),
          postCode = Some(testBusinessPostcode)
        ))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CapturePartnershipCompanyNumberController.show().url)
      }
    }
    "partnership type is LP and partnership type is missing" should {
      "go to capture partnership utr page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.submit(testPostRequest(
          businessEntity = Some(LimitedPartnership),
          entityType = None,
          crn = Some(testCompanyNumber),
          sautr = Some(testSaUtr),
          postCode = Some(testBusinessPostcode)
        ))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CapturePartnershipCompanyNumberController.show().url)
      }
    }
    "partnership utr is missing" should {
      "go to capture partnership utr page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.submit(testPostRequest(
          businessEntity = Some(LimitedPartnership),
          entityType = Some(PartnershipEntityType.LimitedPartnership),
          sautr = None,
          crn = Some(testCompanyNumber),
          postCode = Some(testBusinessPostcode)
        ))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CapturePartnershipUtrController.show().url)
      }
    }
    "post code is missing" should {
      "go to capture partnership utr page" in {
        mockAuthAdminRole()

        val result = TestCheckYourAnswersController.submit(testPostRequest(
          businessEntity = Some(LimitedPartnership),
          entityType = Some(PartnershipEntityType.LimitedPartnership),
          sautr = Some(testSaUtr),
          crn = Some(testCompanyNumber),
          postCode = None
        ))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.PrincipalPlacePostCodeController.show().url)
      }
    }
  }
}
