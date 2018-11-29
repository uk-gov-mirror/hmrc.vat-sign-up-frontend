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
import play.api.libs.json.Json
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.CitizenDetailsStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class SoleTraderResolverControllerISpec extends ComponentSpecBase with CustomMatchers {
  "GET /sole-trader-resolver" when {
    "the user has a NINO on their auth profile" when {
      "citizen details successfully returns the user details" should {
        "redirect to confirm your retrieved user details with a nino source of AuthProfile" in {
          stubAuth(OK, Json.obj(
            "allEnrolments" -> Seq.empty[Enrolment],
            "credentialRole" -> "Admin",
            "nino" -> testNino
          ))
          stubGetCitizenDetailsByNino(testNino)(OK, testUserDetails)

          val res = get("/sole-trader-resolver")

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.ConfirmYourRetrievedUserDetailsController.show().url)
          )
        }
      }
    }
    "the user has an SAUTR enrolment" when {
      "citizen details successfully returns the user details" should {
        "redirect to confirm your retrieved user details with a nino source of AuthProfile" in {
          stubAuth(OK, Json.obj(
            "allEnrolments" -> Seq(irsaEnrolment),
            "credentialRole" -> "Admin"
          ))
          stubGetCitizenDetailsBySautr(testSaUtr)(OK, testUserDetails)

          val res = get("/sole-trader-resolver")

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.ConfirmYourRetrievedUserDetailsController.show().url)
          )
        }
      }
    }
    "the user has no enrolment or NINO" should {
      "redirect to Capture your details" in {
        stubAuth(OK, Json.obj(
          "allEnrolments" -> Seq.empty[Enrolment],
          "credentialRole" -> "Admin"
        ))

        val res = get("/sole-trader-resolver")

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureYourDetailsController.show().url)
        )
      }
    }
  }
}
