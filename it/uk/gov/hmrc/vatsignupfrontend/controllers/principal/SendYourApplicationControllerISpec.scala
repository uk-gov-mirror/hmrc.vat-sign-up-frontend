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

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.ReSignUpJourney
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.SubmissionStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class SendYourApplicationControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /about-to-submit" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse(vatDecEnrolment))

      val res = get("/about-to-submit")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /about-to-submit" when {
    "Submission is successful" should {
      "Submit successfully and redirect to information received" in {
        stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
        stubMigratedSubmissionSuccess()

        val res = post("/about-to-submit", cookies = Map(
          SessionKeys.vatNumberKey -> testVatNumber
        ))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(resignup.routes.SignUpCompleteController.show().url)
        )
      }

      "Submission is unsuccessful" should {
        "return INTERNAL_SERVER_ERROR" in {
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubMigratedSubmissionFailure()

          val res = post("/about-to-submit", cookies = Map(
            SessionKeys.vatNumberKey -> testVatNumber
          ))()

          res should have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }
      }

      "Submission is unsuccessful when no VRN" should {
        "redirect to ResolveVatNumberController" in {
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubMigratedSubmissionFailure()

          val res = post("/about-to-submit")()

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.ResolveVatNumberController.resolve().url)
          )
        }
      }
    }
  }
}
