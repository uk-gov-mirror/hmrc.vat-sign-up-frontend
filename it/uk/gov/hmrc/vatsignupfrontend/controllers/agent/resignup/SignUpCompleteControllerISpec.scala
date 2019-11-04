
package uk.gov.hmrc.vatsignupfrontend.controllers.agent.resignup

import play.api.http.Status.{OK, SEE_OTHER}
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.routes._
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants.testVatNumber
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub.{agentEnrolment, stubAuth, successfulAuthResponse}
import uk.gov.hmrc.vatsignupfrontend.models.SoleTrader

class SignUpCompleteControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /sign-up-complete" when {
    "the VAT number and business entity are in session" should {
      "show the information received page" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = get("/client/sign-up-complete", Map(
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.businessEntityKey -> SoleTrader.toString
        ))

        res should have(httpStatus(OK))
      }
    }
    "the VAT number is in session and the business entity is NOT in session " should {
      "redirect to Capture VAT number" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = get("/client/sign-up-complete", Map(
          SessionKeys.vatNumberKey -> testVatNumber
        ))

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(CaptureVatNumberController.show().url)
        )
      }
    }
    "the VAT number is NOT in session and the business entity is in session " should {
      "redirect to Capture VAT number" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = get("/client/sign-up-complete", Map(
          SessionKeys.businessEntityKey -> SoleTrader.toString
        ))

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(CaptureVatNumberController.show().url)
        )
      }
    }
    "neither the VAT number or business entity are in session " should {
      "redirect to Capture VAT number" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = get("/client/sign-up-complete")

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(CaptureVatNumberController.show().url)
        )
      }
    }
  }

}