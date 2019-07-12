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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.FinalCheckYourAnswer
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.connectors.mocks.MockSubscriptionRequestSummaryConnector
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.SubscriptionRequestSummaryHttpParser.{SubscriptionRequestExistsButNotComplete, SubscriptionRequestUnexpectedError}
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.models.companieshouse.{LimitedLiabilityPartnership, NonPartnershipEntity}
import uk.gov.hmrc.vatsignupfrontend.services.mocks.{MockGetCompanyNameService, MockStoreVatNumberService, MockSubmissionService}

import scala.concurrent.Future

class CheckYourAnswersFinalControllerSpec extends UnitSpec with GuiceOneAppPerSuite
  with MockControllerComponents
  with MockStoreVatNumberService
  with MockSubmissionService
  with MockSubscriptionRequestSummaryConnector
  with MockGetCompanyNameService {

  object TestCheckYourAnswersFinalController
    extends CheckYourAnswersFinalController(
      mockControllerComponents, mockStoreVatNumberService, mockSubscriptionRequestSummaryConnector, mockSubmissionService, mockGetCompanyNameService
    )

  override def beforeEach(): Unit = {
    enable(FinalCheckYourAnswer)
  }

  def testGetRequest(vatNumber: Option[String] = Some(testVatNumber)): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("GET", "/client/check-your-answers-final").withSession(
      SessionKeys.vatNumberKey -> vatNumber.getOrElse("")
    )

  def testPostRequest(vatNumber: Option[String] = Some(testVatNumber)): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/client/check-your-answers-final").withSession(
      SessionKeys.vatNumberKey -> vatNumber.getOrElse("")
    )

  "Show action on the Check Your Answer Final Controller" when {
    "vat number is missing from session" should {
      "go to Capture Vat page" in {
        mockAuthAdminRole()

        val res = TestCheckYourAnswersFinalController.show(testGetRequest(vatNumber = None))
        status(res) shouldBe SEE_OTHER
        redirectLocation(res) shouldBe Some(routes.CaptureVatNumberController.show().url)
      }
    }
    "vat number is in session" should {
      "subscription request summary returns internal server error" should {
        "return Internal Server Error" in {
          mockAuthAdminRole()
          mockGetSubscriptionRequest(testVatNumber)(
            Future.successful(Left(SubscriptionRequestUnexpectedError(INTERNAL_SERVER_ERROR, "Unexpected status from Backend")))
          )
          intercept[InternalServerException](await(TestCheckYourAnswersFinalController.show(testGetRequest())))
        }
      }
      "subscription request summary returns other errors regarding data" should {
        "redirect to Capture VAT page - restart the journey" in {
          mockAuthAdminRole()
          mockGetSubscriptionRequest(testVatNumber)(
            Future.successful(Left(SubscriptionRequestExistsButNotComplete))
          )

          val res = TestCheckYourAnswersFinalController.show(testGetRequest())
          status(res) shouldBe SEE_OTHER
          redirectLocation(res) shouldBe Some(routes.CaptureVatNumberController.show().url)

        }
      }

      "subscription request summary returns data" should {
        "subscription request summary contains a company number" should {
          "get company name service returns NOT_FOUND error" should {
            "return Internal Server Error" in {
              mockAuthAdminRole()
              mockGetSubscriptionRequest(testVatNumber)(
                Future.successful(Right(
                  SubscriptionRequestSummary(testVatNumber, LimitedCompany, None, Some(testCompanyNumber), None, None, testEmail, Digital)))
              )
              mockGetCompanyNameNotFound(testCompanyNumber)
              intercept[InternalServerException](await(TestCheckYourAnswersFinalController.show(testGetRequest())))

            }
          }
          "get company name service returns INTERNAL_SERVER_ERROR" should {
            "return Internal Server Error" in {
              mockAuthAdminRole()
              mockGetSubscriptionRequest(testVatNumber)(
                Future.successful(Right(
                  SubscriptionRequestSummary(testVatNumber, LimitedCompany, None, Some(testCompanyNumber), None, None, testEmail, Digital)))
              )
              mockGetCompanyNameFailure(testCompanyNumber)
              intercept[InternalServerException](await(TestCheckYourAnswersFinalController.show(testGetRequest())))

            }
          }
          "get company name service returns data" should {
            "display the Check Your Answers Final Page" should {
              "for Limited Company" in {
                mockAuthAdminRole()
                mockGetSubscriptionRequest(testVatNumber)(
                  Future.successful(Right(
                    SubscriptionRequestSummary(testVatNumber, LimitedCompany, None, Some(testCompanyNumber), None, None, testEmail, Digital)))
                )
                mockGetCompanyNameSuccess(testCompanyNumber, NonPartnershipEntity)

                val res = TestCheckYourAnswersFinalController.show(testGetRequest())
                status(res) shouldBe OK
              }
              "for Limited Partnership" in {
                mockAuthAdminRole()
                mockGetSubscriptionRequest(testVatNumber)(
                  Future.successful(Right(
                    SubscriptionRequestSummary(testVatNumber, LimitedPartnership, None, Some(testCompanyNumber), None, None, testEmail, Digital)))
                )
                mockGetCompanyNameSuccess(testCompanyNumber, LimitedLiabilityPartnership)

                val res = TestCheckYourAnswersFinalController.show(testGetRequest())
                status(res) shouldBe OK
              }
              "for Registered Society" in {
                mockAuthAdminRole()
                mockGetSubscriptionRequest(testVatNumber)(
                  Future.successful(Right(
                    SubscriptionRequestSummary(testVatNumber, RegisteredSociety, None, Some(testCompanyNumber), None, None, testEmail, Digital)))
                )
                mockGetCompanyNameSuccess(testCompanyNumber, NonPartnershipEntity)

                val res = TestCheckYourAnswersFinalController.show(testGetRequest())
                status(res) shouldBe OK
              }
            }
          }
        }
        "subscription request summary doesn't contain a company number" should {
          "display the Check Your Answers Final Page" should {
            "for Sole Trader" in {
              mockAuthAdminRole()
              mockGetSubscriptionRequest(testVatNumber)(
                Future.successful(Right(
                  SubscriptionRequestSummary(testVatNumber, SoleTrader, Some(testNino), None, None, None, testEmail, Digital)))
              )

              val res = TestCheckYourAnswersFinalController.show(testGetRequest())
              status(res) shouldBe OK
            }
            "for General Partnership" in {
              mockAuthAdminRole()
              mockGetSubscriptionRequest(testVatNumber)(
                Future.successful(Right(
                  SubscriptionRequestSummary(testVatNumber, GeneralPartnership, None, None, Some(testSaUtr), None, testEmail, Digital)))
              )
              val res = TestCheckYourAnswersFinalController.show(testGetRequest())
              status(res) shouldBe OK
            }
          }
        }
      }
    }

  }
  "Submit action on the Check Your Answer Final Controller" when {

    "submission service returns a successful submission response" should {
      "redirect to the confirmation controller" in {
        mockAuthAdminRole()
        mockSubmitSuccess(testVatNumber)

        val res = TestCheckYourAnswersFinalController.submit(testPostRequest())
        status(res) shouldBe SEE_OTHER
        redirectLocation(res) shouldBe Some(routes.ConfirmationController.show().url)
      }
    }

    "submission service fails" should {
      "goto technical difficulties" in {
        mockAuthAdminRole()
        mockSubmitFailure(testVatNumber)

        intercept[InternalServerException] {
          await(TestCheckYourAnswersFinalController.submit(testPostRequest()))
        }
      }
    }
  }


}