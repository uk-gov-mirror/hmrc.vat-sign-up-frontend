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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.soletrader

import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreNinoStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class SoleTraderResolverControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /sole-trader-resolver" when {
    "the SkipCidCheck feature switch is enabled" when {
      "the user has a NINO on their profile & a VAT number in session" should {
        "Redirect to DirectDebitResolverController" in {
          stubAuth(OK, Json.obj(
            "allEnrolments" -> Seq(irsaEnrolment),
            "credentialRole" -> "Admin",
            "nino" -> testNino
          ))

          stubStoreNinoSuccess(testVatNumber, testNino)

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
  }
}