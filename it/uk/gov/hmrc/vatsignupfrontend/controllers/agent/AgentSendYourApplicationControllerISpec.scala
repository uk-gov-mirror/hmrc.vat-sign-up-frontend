
package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.SubmissionStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class AgentSendYourApplicationControllerISpec extends ComponentSpecBase with CustomMatchers {


  "GET /client/about-to-submit" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/about-to-submit")

      res should have(
        httpStatus(OK)
      )
    }

    "POST /client/about-to-submit" when {
      "Submission is successful" should {
        "Submit successfully and redirect to information received" in {
          stubAuth(OK, successfulAuthResponse(agentEnrolment))
          stubMigratedSubmissionSuccess()

          val res = post("/client/about-to-submit", cookies = Map(
            SessionKeys.vatNumberKey -> testVatNumber
          ))()

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(resignup.routes.SignUpCompleteController.show().url)
          )
        }

        "Submission is unsuccessful" should {
          "return INTERNAL_SERVER_ERROR" in {
            stubAuth(OK, successfulAuthResponse(agentEnrolment))
            stubMigratedSubmissionFailure()

            val res = post("/client/about-to-submit", cookies = Map(
              SessionKeys.vatNumberKey -> testVatNumber
            ))()

            res should have(
              httpStatus(INTERNAL_SERVER_ERROR)
            )
          }
        }

        "Submission is unsuccessful when no VRN" should {
          "redirect to ResolveVatNumberController" in {
            stubAuth(OK, successfulAuthResponse(agentEnrolment))
            stubMigratedSubmissionFailure()

            val res = post("/client/about-to-submit")()

            res should have(
              httpStatus(SEE_OTHER),
              redirectUri(routes.CaptureVatNumberController.show().url)
            )
          }
        }
      }
    }
  }
}