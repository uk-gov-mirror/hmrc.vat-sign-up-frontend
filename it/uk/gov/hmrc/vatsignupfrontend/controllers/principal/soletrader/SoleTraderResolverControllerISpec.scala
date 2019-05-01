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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.soletrader

import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.SkipCidCheck
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.CitizenDetailsStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreNinoStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.AuthProfile

class SoleTraderResolverControllerISpec extends ComponentSpecBase with CustomMatchers {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(SkipCidCheck)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    disable(SkipCidCheck)
  }

  "GET /sole-trader-resolver" when {
    "the SkipCidCheck feature switch is enabled" when {
      "the user has a NINO on their profile & a VAT number in session" should {
        "Redirect to DirectDebitResolverController" in {
          stubAuth(OK, Json.obj(
            "allEnrolments" -> Seq(irsaEnrolment),
            "credentialRole" -> "Admin",
            "nino" -> testNino
          ))

          stubStoreNinoSuccess(testVatNumber, testNino, AuthProfile)

          val res = get("/sole-trader-resolver", Map(
            SessionKeys.vatNumberKey -> testVatNumber
          ))

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(principalRoutes.DirectDebitResolverController.show().url)
          )
        }
      }

      "the user doesn't have a NINO on their profile" should {
        "redirect to CaptureNinoController" in {
          stubAuth(OK, Json.obj(
            "allEnrolments" -> Seq(irsaEnrolment),
            "credentialRole" -> "Admin"
          ))

          val res = get("/sole-trader-resolver", Map(
            SessionKeys.vatNumberKey -> testVatNumber
          ))

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CaptureNinoController.show().url)
          )
        }
      }

      "there is no VAT number in the user's session" should {
        "redirect to CaptureVatNumberController" in {
          stubAuth(OK, Json.obj(
            "allEnrolments" -> Seq(irsaEnrolment),
            "credentialRole" -> "Admin",
            "nino" -> testNino
          ))

          val res = get("/sole-trader-resolver")

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(principalRoutes.CaptureVatNumberController.show().url)
          )
        }
      }
    }

    "the SkipCidCheck feature switch is disabled" when {
      "the user has a NINO on their auth profile" when {
        "citizen details successfully returns the user details" should {
          "redirect to confirm your retrieved user details with a nino source of AuthProfile" in {
            disable(SkipCidCheck)

            stubAuth(OK, Json.obj(
              "allEnrolments" -> Seq.empty[Enrolment],
              "credentialRole" -> "Admin",
              "nino" -> testNino
            ))
            stubGetCitizenDetailsByNino(testNino)(OK, testUserDetails)

            val res = get("/sole-trader-resolver")

            res should have(
              httpStatus(SEE_OTHER),
              redirectUri(principalRoutes.ConfirmYourRetrievedUserDetailsController.show().url)
            )
          }
        }
      }
      "the user has an SAUTR enrolment" when {
        "citizen details successfully returns the user details" should {
          "redirect to confirm your retrieved user details with a nino source of AuthProfile" in {
            disable(SkipCidCheck)

            stubAuth(OK, Json.obj(
              "allEnrolments" -> Seq(irsaEnrolment),
              "credentialRole" -> "Admin"
            ))
            stubGetCitizenDetailsBySautr(testSaUtr)(OK, testUserDetails)

            val res = get("/sole-trader-resolver")

            res should have(
              httpStatus(SEE_OTHER),
              redirectUri(principalRoutes.ConfirmYourRetrievedUserDetailsController.show().url)
            )
          }
        }
      }
      "the user has no enrolment or NINO" should {
        "redirect to Capture your details" in {
          disable(SkipCidCheck)

          stubAuth(OK, Json.obj(
            "allEnrolments" -> Seq.empty[Enrolment],
            "credentialRole" -> "Admin"
          ))

          val res = get("/sole-trader-resolver")

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(principalRoutes.CaptureYourDetailsController.show().url)
          )
        }
      }
    }
  }

}
