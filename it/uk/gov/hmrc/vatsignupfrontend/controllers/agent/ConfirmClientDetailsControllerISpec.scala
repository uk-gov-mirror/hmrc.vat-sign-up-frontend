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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import java.time.LocalDate
import java.util.UUID

import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreNinoStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.{DateModel, UserDetailsModel, UserEntered}

class ConfirmClientDetailsControllerISpec extends ComponentSpecBase with CustomMatchers {

  val testUserDetails: UserDetailsModel = UserDetailsModel(
    firstName = UUID.randomUUID().toString,
    lastName = UUID.randomUUID().toString,
    nino = testNino,
    dateOfBirth = DateModel.dateConvert(LocalDate.now())
  )

  val testUserDetailsJson: String = Json.toJson(testUserDetails).toString()

  "GET /confirm-client" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/confirm-client", Map(SessionKeys.vatNumberKey -> testVatNumber, SessionKeys.userDetailsKey -> testUserDetailsJson))

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /confirm-client" when {
    "store nino is successful" should {
      "redirect to EmailRoutingController" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStoreNinoSuccess(testVatNumber, testUserDetails, UserEntered)

        val res = post("/client/confirm-client", Map(SessionKeys.vatNumberKey -> testVatNumber, SessionKeys.userDetailsKey -> testUserDetailsJson))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.EmailRoutingController.route().url)
        )
      }
    }

    "store nino returned no match" should {
      "redirect to the failed client matching page" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStoreNinoNoMatch(testVatNumber, testUserDetails, UserEntered)

        val res = post("/client/confirm-client", Map(SessionKeys.vatNumberKey -> testVatNumber, SessionKeys.userDetailsKey -> testUserDetailsJson))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.FailedClientMatchingController.show().url)
        )
      }
    }
  }

}
