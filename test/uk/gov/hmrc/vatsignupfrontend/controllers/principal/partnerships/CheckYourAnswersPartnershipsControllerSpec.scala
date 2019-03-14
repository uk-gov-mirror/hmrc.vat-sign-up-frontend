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
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys._
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{GeneralPartnershipJourney, JointVenturePropertyJourney, LimitedPartnershipJourney}
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreJointVentureInformationHttpParser.{StoreJointVentureInformationFailureResponse, StoreJointVentureInformationSuccess}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StorePartnershipInformationHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models.PartnershipEntityType.CompanyTypeSessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models.YesNo.YesNoSessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models.{PartnershipEntityType, _}
import uk.gov.hmrc.vatsignupfrontend.services.mocks.{MockStoreJointVentureInformationService, MockStorePartnershipInformationService}
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils.jsonSessionFormatter

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
                      entityType: Option[PartnershipEntityType] = None,
                      jointVentureProperty: Option[YesNo] = None
                     ): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/check-your-answers").withSession(
      sessionValues(vatNumber, sautr, crn, postCode, entityType, businessEntity, jointVentureProperty).toSeq: _*
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

    "Partnership Entity is General Partnership" when {

      "the General Partnership feature is enabled" when {

        "the Joint Venture or Property Partnership feature is enabled" when {

          "The partnership is Joint Venture or Property" when {

            "All prerequisite data is in session" when {

              "the storeJointVenturePropertyInformation is successful" should {

                "Redirect to the Direct Debit resolver" in {
                  enable(GeneralPartnershipJourney)
                  enable(JointVenturePropertyJourney)

                  mockAuthAdminRole()
                  mockStoreJointVentureInformation(testVatNumber)(Right(StoreJointVentureInformationSuccess))

                  val result = await(TestCheckYourAnswersController.submit(testPostRequest(
                    businessEntity = Some(GeneralPartnership),
                    jointVentureProperty = Some(Yes)
                  )))
                  status(result) shouldBe Status.SEE_OTHER
                  redirectLocation(result) should contain(principalRoutes.DirectDebitResolverController.show().url)
                }
              }

              "the storeJointVenturePropertyInformation fails" should {

                "throw an internal server error exception" in {
                  enable(GeneralPartnershipJourney)
                  enable(JointVenturePropertyJourney)

                  mockAuthAdminRole()
                  mockStoreJointVentureInformation(testVatNumber)(Left(StoreJointVentureInformationFailureResponse(BAD_REQUEST)))

                  lazy val result = await(TestCheckYourAnswersController.submit(testPostRequest(
                    businessEntity = Some(GeneralPartnership),
                    jointVentureProperty = Some(Yes)
                  )))

                  intercept[InternalServerException](result)
                }
              }
            }

            "VRN is missing from session" should {

              "Redirect to VRN Capture page" in {
                enable(GeneralPartnershipJourney)
                enable(JointVenturePropertyJourney)

                mockAuthAdminRole()

                val result = await(TestCheckYourAnswersController.submit(testPostRequest(
                  vatNumber = None,
                  businessEntity = Some(GeneralPartnership),
                  jointVentureProperty = Some(Yes)
                )))
                status(result) shouldBe Status.SEE_OTHER
                redirectLocation(result) should contain(principalRoutes.ResolveVatNumberController.resolve().url)
              }
            }

            "Business Entity is missing from session" should {

              "Redirect to Capture Business Entity page" in {
                enable(GeneralPartnershipJourney)
                enable(JointVenturePropertyJourney)

                mockAuthAdminRole()

                val result = await(TestCheckYourAnswersController.submit(testPostRequest(
                  jointVentureProperty = Some(Yes)
                )))
                status(result) shouldBe Status.SEE_OTHER
                redirectLocation(result) should contain(principalRoutes.CaptureBusinessEntityController.show().url)
              }
            }
          }

          "The partnership is NOT Joint Venture or Property" when {

            "All prerequisite data is in session" when {

              "the storeJointVenturePropertyInformation is successful" should {

                "Redirect to the Direct Debit resolver" in {
                  enable(GeneralPartnershipJourney)
                  enable(JointVenturePropertyJourney)

                  mockAuthAdminRole()
                  mockStorePartnershipInformation(
                    vatNumber = testVatNumber,
                    sautr = testSaUtr,
                    postCode = Some(testBusinessPostcode)
                  )(Right(StorePartnershipInformationSuccess))

                  val result = await(TestCheckYourAnswersController.submit(testPostRequest(
                    businessEntity = Some(GeneralPartnership),
                    jointVentureProperty = Some(No),
                    sautr = Some(testSaUtr),
                    postCode = Some(testBusinessPostcode)
                  )))
                  status(result) shouldBe Status.SEE_OTHER
                  redirectLocation(result) should contain(principalRoutes.DirectDebitResolverController.show().url)
                }
              }

              "the storeJointVenturePropertyInformation fails" should {

                "throw an internal server error exception" in {
                  enable(GeneralPartnershipJourney)
                  enable(JointVenturePropertyJourney)

                  mockAuthAdminRole()
                  mockStorePartnershipInformation(
                    vatNumber = testVatNumber,
                    sautr = testSaUtr,
                    postCode = Some(testBusinessPostcode)
                  )(Left(StorePartnershipInformationFailureResponse(BAD_REQUEST)))

                  lazy val result = await(TestCheckYourAnswersController.submit(testPostRequest(
                    businessEntity = Some(GeneralPartnership),
                    jointVentureProperty = Some(No),
                    sautr = Some(testSaUtr),
                    postCode = Some(testBusinessPostcode)
                  )))

                  intercept[InternalServerException](result)
                }
              }
            }

            "VRN is missing from session" should {

              "Redirect to Resolve VAT Number controller" in {
                enable(GeneralPartnershipJourney)
                enable(JointVenturePropertyJourney)

                mockAuthAdminRole()

                val result = await(TestCheckYourAnswersController.submit(testPostRequest(
                  businessEntity = Some(GeneralPartnership),
                  jointVentureProperty = Some(No),
                  vatNumber = None,
                  sautr = Some(testSaUtr),
                  postCode = Some(testBusinessPostcode)
                )))

                status(result) shouldBe Status.SEE_OTHER
                redirectLocation(result) should contain(principalRoutes.ResolveVatNumberController.resolve().url)
              }
            }

            "Business Entity is missing from session" should {

              "Redirect to Capture Business Entity page" in {
                enable(GeneralPartnershipJourney)
                enable(JointVenturePropertyJourney)

                mockAuthAdminRole()

                val result = await(TestCheckYourAnswersController.submit(testPostRequest(
                  jointVentureProperty = Some(No),
                  sautr = Some(testSaUtr),
                  postCode = Some(testBusinessPostcode)
                )))

                status(result) shouldBe Status.SEE_OTHER
                redirectLocation(result) should contain(principalRoutes.CaptureBusinessEntityController.show().url)
              }
            }

            "SA UTR is missing from session" should {

              "Redirect to Partnership SA UTR capture page" in {
                enable(GeneralPartnershipJourney)
                enable(JointVenturePropertyJourney)

                mockAuthAdminRole()

                val result = await(TestCheckYourAnswersController.submit(testPostRequest(
                  businessEntity = Some(GeneralPartnership),
                  jointVentureProperty = Some(No),
                  postCode = Some(testBusinessPostcode)
                )))

                status(result) shouldBe Status.SEE_OTHER
                redirectLocation(result) should contain(routes.CapturePartnershipUtrController.show().url)
              }
            }

            "Business PostCode is missing from session" should {

              "Redirect to Partnership SA UTR capture page" in {
                enable(GeneralPartnershipJourney)
                enable(JointVenturePropertyJourney)

                mockAuthAdminRole()

                val result = await(TestCheckYourAnswersController.submit(testPostRequest(
                  businessEntity = Some(GeneralPartnership),
                  jointVentureProperty = Some(No),
                  sautr = Some(testSaUtr)
                )))

                status(result) shouldBe Status.SEE_OTHER
                redirectLocation(result) should contain(routes.PrincipalPlacePostCodeController.show().url)
              }
            }
          }

          "Joint Venture or Property answer is missing from session" should {

            "Redirect Joint Venture or Property  page" in {
              enable(GeneralPartnershipJourney)
              enable(JointVenturePropertyJourney)

              mockAuthAdminRole()

              val result = await(TestCheckYourAnswersController.submit(testPostRequest(
                businessEntity = Some(GeneralPartnership)
              )))

              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) should contain(routes.JointVentureOrPropertyController.show().url)
            }
          }
        }

        "the Joint Venture or Property Partnership feature is disabled" when {

          "All prerequisite data is held in session" when {

            "storePartnershipInformation is successful" should {

              "Redirect to the Direct Debit resolver" in {
                enable(GeneralPartnershipJourney)
                disable(JointVenturePropertyJourney)

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

            "storePartnershipInformation fails due to Known Facts Failure" should {

              "Redirect to the Known Facts Error page" in {
                enable(GeneralPartnershipJourney)
                disable(JointVenturePropertyJourney)

                mockAuthAdminRole()
                mockStorePartnershipInformation(
                  vatNumber = testVatNumber,
                  sautr = testSaUtr,
                  postCode = Some(testBusinessPostcode)
                )(Left(StorePartnershipKnownFactsFailure))

                val result = await(TestCheckYourAnswersController.submit(testPostRequest(
                  businessEntity = Some(GeneralPartnership),
                  sautr = Some(testSaUtr),
                  postCode = Some(testBusinessPostcode)
                )))

                status(result) shouldBe Status.SEE_OTHER
                redirectLocation(result) should contain(routes.CouldNotConfirmKnownFactsController.show().url)
              }
            }

            "storePartnershipInformation fails due to Partnership Utr NotFound" should {

              "Redirect to the Known Facts Error page" in {
                enable(GeneralPartnershipJourney)
                disable(JointVenturePropertyJourney)

                mockAuthAdminRole()
                mockStorePartnershipInformation(
                  vatNumber = testVatNumber,
                  sautr = testSaUtr,
                  postCode = Some(testBusinessPostcode)
                )(Left(PartnershipUtrNotFound))

                val result = await(TestCheckYourAnswersController.submit(testPostRequest(
                  businessEntity = Some(GeneralPartnership),
                  sautr = Some(testSaUtr),
                  postCode = Some(testBusinessPostcode)
                )))

                status(result) shouldBe Status.SEE_OTHER
                redirectLocation(result) should contain(routes.CouldNotConfirmKnownFactsController.show().url)
              }
            }

            "storePartnershipInformation fails with other exception" should {

              "throw an Internal Server Exception" in {
                enable(GeneralPartnershipJourney)
                disable(JointVenturePropertyJourney)

                mockAuthAdminRole()
                mockStorePartnershipInformation(
                  vatNumber = testVatNumber,
                  sautr = testSaUtr,
                  postCode = Some(testBusinessPostcode)
                )(Left(StorePartnershipInformationFailureResponse(BAD_REQUEST)))

                lazy val result = await(TestCheckYourAnswersController.submit(testPostRequest(
                  businessEntity = Some(GeneralPartnership),
                  sautr = Some(testSaUtr),
                  postCode = Some(testBusinessPostcode)
                )))

                intercept[InternalServerException](result)
              }
            }

          }

          "VRN is missing from session" should {

            "Redirect to Resolve VAT Number controller" in {
              enable(GeneralPartnershipJourney)
              disable(JointVenturePropertyJourney)

              mockAuthAdminRole()

              val result = await(TestCheckYourAnswersController.submit(testPostRequest(
                businessEntity = Some(GeneralPartnership),
                jointVentureProperty = Some(No),
                vatNumber = None,
                sautr = Some(testSaUtr),
                postCode = Some(testBusinessPostcode)
              )))

              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) should contain(principalRoutes.ResolveVatNumberController.resolve().url)
            }
          }

          "Business Entity is missing from session" should {

            "Redirect to Capture Business Entity page" in {
              enable(GeneralPartnershipJourney)
              disable(JointVenturePropertyJourney)

              mockAuthAdminRole()

              val result = await(TestCheckYourAnswersController.submit(testPostRequest(
                jointVentureProperty = Some(No),
                sautr = Some(testSaUtr),
                postCode = Some(testBusinessPostcode)
              )))

              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) should contain(principalRoutes.CaptureBusinessEntityController.show().url)
            }
          }

          "SA UTR is missing from session" should {

            "Redirect to Partnership SA UTR capture page" in {
              enable(GeneralPartnershipJourney)
              disable(JointVenturePropertyJourney)

              mockAuthAdminRole()

              val result = await(TestCheckYourAnswersController.submit(testPostRequest(
                businessEntity = Some(GeneralPartnership),
                jointVentureProperty = Some(No),
                postCode = Some(testBusinessPostcode)
              )))

              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) should contain(routes.CapturePartnershipUtrController.show().url)
            }
          }

          "Business PostCode is missing from session" should {

            "Redirect to Partnership SA UTR capture page" in {
              enable(GeneralPartnershipJourney)
              disable(JointVenturePropertyJourney)

              mockAuthAdminRole()

              val result = await(TestCheckYourAnswersController.submit(testPostRequest(
                businessEntity = Some(GeneralPartnership),
                jointVentureProperty = Some(No),
                sautr = Some(testSaUtr)
              )))

              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) should contain(routes.PrincipalPlacePostCodeController.show().url)
            }
          }
        }
      }
    }

    "Partnership Entity is of type Limited Partnership" when {

      "the Limited Partnership feature is enabled" when {

        "All prerequisite data is in session" should {

          "storePartnershipInformation is successful" should {

            "Redirect to the Direct Debit resolver" in {

              mockAuthAdminRole()
              enable(LimitedPartnershipJourney)

              mockStorePartnershipInformation(
                vatNumber = testVatNumber,
                sautr = testSaUtr,
                companyNumber = testCompanyNumber,
                partnershipEntity = PartnershipEntityType.LimitedPartnership,
                postCode = Some(testBusinessPostcode)
              )(Right(StorePartnershipInformationSuccess))

              val result = TestCheckYourAnswersController.submit(testPostRequest(
                businessEntity = Some(LimitedPartnership),
                entityType = Some(PartnershipEntityType.LimitedPartnership),
                sautr = Some(testSaUtr),
                postCode = Some(testBusinessPostcode),
                crn = Some(testCompanyNumber)
              ))

              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) should contain(principalRoutes.DirectDebitResolverController.show().url)
            }
          }

          "storePartnershipInformation fails due to Known Facts Failure" should {

            "Redirect to the Known Facts Error page" in {
              enable(GeneralPartnershipJourney)
              disable(JointVenturePropertyJourney)

              mockAuthAdminRole()
              mockStorePartnershipInformation(
                vatNumber = testVatNumber,
                sautr = testSaUtr,
                companyNumber = testCompanyNumber,
                partnershipEntity = PartnershipEntityType.LimitedPartnership,
                postCode = Some(testBusinessPostcode)
              )(Left(StorePartnershipKnownFactsFailure))

              val result = TestCheckYourAnswersController.submit(testPostRequest(
                businessEntity = Some(LimitedPartnership),
                entityType = Some(PartnershipEntityType.LimitedPartnership),
                sautr = Some(testSaUtr),
                postCode = Some(testBusinessPostcode),
                crn = Some(testCompanyNumber)
              ))

              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) should contain(routes.CouldNotConfirmKnownFactsController.show().url)
            }
          }

          "storePartnershipInformation fails due to Partnership Utr NotFound" should {

            "Redirect to the Known Facts Error page" in {
              enable(GeneralPartnershipJourney)
              disable(JointVenturePropertyJourney)

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
                sautr = Some(testSaUtr),
                postCode = Some(testBusinessPostcode),
                crn = Some(testCompanyNumber)
              )))

              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) should contain(routes.CouldNotConfirmKnownFactsController.show().url)
            }
          }

          "storePartnershipInformation fails with other exception" should {

            "throw an Internal Server Exception" in {
              enable(GeneralPartnershipJourney)
              disable(JointVenturePropertyJourney)

              mockAuthAdminRole()
              mockStorePartnershipInformation(
                vatNumber = testVatNumber,
                sautr = testSaUtr,
                companyNumber = testCompanyNumber,
                partnershipEntity = PartnershipEntityType.LimitedPartnership,
                postCode = Some(testBusinessPostcode)
              )(Left(StorePartnershipInformationFailureResponse(BAD_REQUEST)))

              lazy val result = await(TestCheckYourAnswersController.submit(testPostRequest(
                businessEntity = Some(LimitedPartnership),
                entityType = Some(PartnershipEntityType.LimitedPartnership),
                sautr = Some(testSaUtr),
                postCode = Some(testBusinessPostcode),
                crn = Some(testCompanyNumber)
              )))

              intercept[InternalServerException](result)
            }
          }
        }

        "VRN is missing from session" should {

          "Redirect to Resolve VAT Number controller" in {
            enable(GeneralPartnershipJourney)
            enable(JointVenturePropertyJourney)

            mockAuthAdminRole()

            val result = await(TestCheckYourAnswersController.submit(testPostRequest(
              businessEntity = Some(LimitedPartnership),
              entityType = Some(PartnershipEntityType.LimitedPartnership),
              vatNumber = None,
              sautr = Some(testSaUtr),
              postCode = Some(testBusinessPostcode),
              crn = Some(testCompanyNumber)
            )))

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(principalRoutes.ResolveVatNumberController.resolve().url)
          }
        }

        "Business Entity is missing from session" should {

          "Redirect to Capture Business Entity page" in {
            enable(GeneralPartnershipJourney)
            enable(JointVenturePropertyJourney)

            mockAuthAdminRole()

            val result = await(TestCheckYourAnswersController.submit(testPostRequest(
              entityType = Some(PartnershipEntityType.LimitedPartnership),
              sautr = Some(testSaUtr),
              postCode = Some(testBusinessPostcode),
              crn = Some(testCompanyNumber)
            )))

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(principalRoutes.CaptureBusinessEntityController.show().url)
          }
        }

        "SA UTR is missing from session" should {

          "Redirect to Partnership SA UTR capture page" in {
            enable(GeneralPartnershipJourney)
            enable(JointVenturePropertyJourney)

            mockAuthAdminRole()

            val result = await(TestCheckYourAnswersController.submit(testPostRequest(
              businessEntity = Some(LimitedPartnership),
              entityType = Some(PartnershipEntityType.LimitedPartnership),
              postCode = Some(testBusinessPostcode),
              crn = Some(testCompanyNumber)
            )))

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(routes.CapturePartnershipUtrController.show().url)
          }
        }

        "Business PostCode is missing from session" should {

          "Redirect to Partnership SA UTR capture page" in {
            enable(GeneralPartnershipJourney)
            enable(JointVenturePropertyJourney)

            mockAuthAdminRole()

            val result = await(TestCheckYourAnswersController.submit(testPostRequest(
              businessEntity = Some(LimitedPartnership),
              entityType = Some(PartnershipEntityType.LimitedPartnership),
              sautr = Some(testSaUtr),
              crn = Some(testCompanyNumber)
            )))

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(routes.PrincipalPlacePostCodeController.show().url)
          }
        }

        "CRN is missing from session" should {

          "Redirect to Capture Partnership Company Number page" in {
            enable(GeneralPartnershipJourney)
            enable(JointVenturePropertyJourney)

            mockAuthAdminRole()

            val result = await(TestCheckYourAnswersController.submit(testPostRequest(
              businessEntity = Some(LimitedPartnership),
              entityType = Some(PartnershipEntityType.LimitedPartnership),
              sautr = Some(testSaUtr),
              postCode = Some(testBusinessPostcode)
            )))

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(routes.CapturePartnershipCompanyNumberController.show().url)
          }
        }

        "Partnership Entity Type is missing from session" should {

          "Redirect to Partnership Company Number page" in {
            enable(GeneralPartnershipJourney)
            enable(JointVenturePropertyJourney)

            mockAuthAdminRole()

            val result = await(TestCheckYourAnswersController.submit(testPostRequest(
              businessEntity = Some(LimitedPartnership),
              sautr = Some(testSaUtr),
              postCode = Some(testBusinessPostcode),
              crn = Some(testCompanyNumber)
            )))

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(routes.CapturePartnershipCompanyNumberController.show().url)
          }
        }
      }
    }
  }
}
