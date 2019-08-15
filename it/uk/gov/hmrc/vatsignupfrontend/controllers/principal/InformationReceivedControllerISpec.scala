
package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.SoleTrader
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants.testVatNumber
import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub.{stubAuth, successfulAuthResponse}

class InformationReceivedControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /information-received" when {
    "the VAT number and business entity are in session" should {
      "show the information received page" in {
        stubAuth(OK, successfulAuthResponse())

        val res = get("/information-received", Map(
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.businessEntityKey -> SoleTrader.toString
        ))

        res should have(httpStatus(OK))
      }
    }
    "the VAT number is in session and the business entity is NOT in session " should {
      "redirect to Capture VAT number" in {
        stubAuth(OK, successfulAuthResponse())

        val res = get("/information-received", Map(
          SessionKeys.vatNumberKey -> testVatNumber
        ))

        res should have (
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureVatNumberController.show().url)
        )
      }
    }
    "the VAT number is NOT in session and the business entity is in session " should {
      "redirect to Capture VAT number" in {
        stubAuth(OK, successfulAuthResponse())

        val res = get("/information-received", Map(
          SessionKeys.businessEntityKey -> SoleTrader.toString
        ))

        res should have (
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureVatNumberController.show().url)
        )
      }
    }
    "neither the VAT number or business entity are in session " should {
      "redirect to Capture VAT number" in {
        stubAuth(OK, successfulAuthResponse())

        val res = get("/information-received")

        res should have (
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureVatNumberController.show().url)
        )
      }
    }
  }

}
