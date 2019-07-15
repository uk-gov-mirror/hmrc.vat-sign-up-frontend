
package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{DirectDebitTermsJourney, FeatureSwitching, FinalCheckYourAnswer}
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.GetCompanyNameStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.SubmissionStub.{stubSubmissionFailure, stubSubmissionSuccess}
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.SubscriptionRequestSummaryStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.models.companieshouse.NonPartnershipEntity

class CheckYourAnswersFinalControllerISpec extends ComponentSpecBase with CustomMatchers with FeatureSwitching {

  "GET /client/check-your-answers-final" when {
    "feature switch is disabled" should {
      "return NOT_FOUND" in {
        disable(FinalCheckYourAnswer)

        val res = get("/client/check-your-answers-final",
          Map(SessionKeys.vatNumberKey -> testVatNumber)
        )

        res should have(
          httpStatus(NOT_FOUND)
        )
      }
    }

    "the subscription request summary returned INTERNAL SERVER ERROR" should {
      "return INTERNAL_SERVER_ERROR" in {
        enable(FinalCheckYourAnswer)
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubGetSubscriptionRequestException(testVatNumber)(INTERNAL_SERVER_ERROR)

        val res = get("/client/check-your-answers-final", Map(SessionKeys.vatNumberKey -> testVatNumber))

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }

    "the subscription request summary returned other error" should {
      "return SEE_OTHER and restart journey" in {
        enable(FinalCheckYourAnswer)
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubGetSubscriptionRequestInvalidJson(testVatNumber)(SEE_OTHER)

        val res = get("/client/check-your-answers-final")

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureVatNumberController.show().url)
        )
      }
    }

    "the subscription request summary returned OK" should {
      "contains a company number" should {
        "the company name service returned INTERNAL SERVER ERROR" should {
          "return INTERNAL_SERVER_ERROR" in {
            val model = SubscriptionRequestSummary(
              vatNumber = testVatNumber,
              businessEntity = LimitedCompany,
              optNino = None,
              optCompanyNumber = Some(testCompanyNumber),
              optSautr = None,
              optSignUpEmail = Some(testEmail),
              transactionEmail = testEmail,
              contactPreference = Digital
            )

            enable(FinalCheckYourAnswer)
            stubAuth(OK, successfulAuthResponse(agentEnrolment))
            stubGetSubscriptionRequest(testVatNumber)(OK, Some(model))
            stubGetCompanyNameCompanyFailure(testCompanyNumber)

            val res = get("/client/check-your-answers-final", Map(SessionKeys.vatNumberKey -> testVatNumber))

            res should have(
              httpStatus(INTERNAL_SERVER_ERROR)
            )
          }
        }
        "the company name service returned NOT FOUND " should {
          "redirect to Capture company Number for Limited Company" in {
            val model = SubscriptionRequestSummary(
              vatNumber = testVatNumber,
              businessEntity = LimitedCompany,
              optNino = None,
              optCompanyNumber = Some(testCompanyNumber),
              optSautr = None,
              optSignUpEmail = Some(testEmail),
              transactionEmail = testEmail,
              contactPreference = Digital
            )

            enable(FinalCheckYourAnswer)
            stubAuth(OK, successfulAuthResponse(agentEnrolment))
            stubGetSubscriptionRequest(testVatNumber)(OK, Some(model))
            stubGetCompanyNameCompanyNotFound(testCompanyNumber)

            val res = get("/client/check-your-answers-final", Map(SessionKeys.vatNumberKey -> testVatNumber))

            res should have(
              httpStatus(INTERNAL_SERVER_ERROR)
            )

          }
        }
        "the company name service returned OK" should {
          "return OK" in {
            val model = SubscriptionRequestSummary(
              vatNumber = testVatNumber,
              businessEntity = LimitedCompany,
              optNino = None,
              optCompanyNumber = Some(testCompanyNumber),
              optSautr = None,
              optSignUpEmail = Some(testEmail),
              transactionEmail = testEmail,
              contactPreference = Digital
            )

            enable(FinalCheckYourAnswer)
            stubAuth(OK, successfulAuthResponse(agentEnrolment))
            stubGetSubscriptionRequest(testVatNumber)(OK, Some(model))
            stubGetCompanyName(testCompanyNumber, NonPartnershipEntity)
            val res = get("/client/check-your-answers-final", Map(SessionKeys.vatNumberKey -> testVatNumber))

            res should have(
              httpStatus(OK)
            )
          }
        }
      }
      "doesn't contain a company number" should {
        "return OK" in {
          val model = SubscriptionRequestSummary(
            vatNumber = testVatNumber,
            businessEntity = SoleTrader,
            optNino = Some(testNino),
            optCompanyNumber = None,
            optSautr = None,
            optSignUpEmail = Some(testEmail),
            transactionEmail = testEmail,
            contactPreference = Digital
          )

          enable(FinalCheckYourAnswer)
          stubAuth(OK, successfulAuthResponse(agentEnrolment))
          stubGetSubscriptionRequest(testVatNumber)(OK, Some(model))

          val res = get("/client/check-your-answers-final", Map(SessionKeys.vatNumberKey -> testVatNumber))

          res should have(
            httpStatus(OK)
          )
        }
      }
    }
  }
  "POST /client/check-your-answers-final" when {
    "the submission was successful" should {
      "redirect to the confirmation page" in {
        enable(FinalCheckYourAnswer)
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubSubmissionSuccess()

        val res = post("/client/check-your-answers-final", cookies = Map(
          SessionKeys.vatNumberKey -> testVatNumber
        ))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.ConfirmationController.show().url)
        )
      }
    }
    "the submission is unsuccessful" should {
      "return INTERNAL_SERVER_ERROR" in {
        enable(FinalCheckYourAnswer)
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubSubmissionFailure()

        val res = post("/client/check-your-answers-final", cookies = Map(
          SessionKeys.vatNumberKey -> testVatNumber
        ))()

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
  }
}
