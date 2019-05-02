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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent.soletrader

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.SkipCidCheck
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.routes._
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants.{testNino, testVatNumber}
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreNinoStub.stubStoreNinoSuccess
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.{SoleTrader, UserEntered}

class ConfirmNinoControllerISpec extends ComponentSpecBase with CustomMatchers {

  val uri = "/client/confirm-national-insurance-number"

  "GET /client/confirm-national-insurance-number" when {
    "the SkipCidCheck feature switch is enabled" should {
      "return OK" in {
        enable(SkipCidCheck)
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = get(uri, Map(
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.ninoKey -> testNino,
          SessionKeys.businessEntityKey -> SoleTrader.toString
        ))

        res should have(
          httpStatus(OK)
        )
      }
    }

    "the SkipCidCheck feature switch is disabled" should {
      "return NOT_FOUND" in {
        disable(SkipCidCheck)
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = get(uri, Map(SessionKeys.ninoKey -> testNino))

        res should have (
          httpStatus(NOT_FOUND)
        )
      }
    }
  }

  "POST /client/confirm-national-insurance-number" should {
    "redirect to DirectDebitResolver" in {
      enable(SkipCidCheck)
      stubAuth(OK, successfulAuthResponse(agentEnrolment))
      stubStoreNinoSuccess(testVatNumber, testNino, UserEntered)

      val res = post(uri, Map(
        SessionKeys.vatNumberKey -> testVatNumber,
        SessionKeys.businessEntityKey -> SoleTrader.toString,
        SessionKeys.ninoKey -> testNino
      ))()

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(CaptureAgentEmailController.show().url)
      )
    }
  }

}
