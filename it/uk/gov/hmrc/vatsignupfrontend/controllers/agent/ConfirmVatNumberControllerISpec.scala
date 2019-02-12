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

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreVatNumberStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers, SessionCookieCrumbler}
import uk.gov.hmrc.vatsignupfrontend.models.MigratableDates

class ConfirmVatNumberControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /confirm-vat-number" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/confirm-vat-number", Map(SessionKeys.vatNumberKey -> testVatNumber))

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /vat-number" should {
    "redirect to the capture client business entity page" when {
      "the vat number is successfully stored" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStoreVatNumberSuccess(isFromBta = false)

        val res = post("/client/confirm-vat-number", Map(SessionKeys.vatNumberKey -> testVatNumber))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureBusinessEntityController.show().url)
        )
      }
    }

    "redirect to the overseas resolver controller" when {
      "the overseas vat number is successfully stored" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStoreVatNumberOverseas(isFromBta = false)

        val res = post("/client/confirm-vat-number", Map(SessionKeys.vatNumberKey -> testVatNumber))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.OverseasResolverController.resolve().url)
        )
      }
    }

    "redirect to no agent client relationship page" when {
      "the vat number is unsuccessfully stored as there is no client agent relationship" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStoreVatNumberNoRelationship(isFromBta = false)

        val res = post("/client/confirm-vat-number", Map(SessionKeys.vatNumberKey -> testVatNumber))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.NoAgentClientRelationshipController.show().url)
        )
      }
    }

    "redirect to cannot use service page" when {
      "the vat number is unsuccessfully stored as the client is ineligible" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStoreVatNumberIneligible(isFromBta = false, migratableDates = MigratableDates())

        val res = post("/client/confirm-vat-number", Map(SessionKeys.vatNumberKey -> testVatNumber))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CannotUseServiceController.show().url)
        )
      }
    }

    "redirect to the sign up after this date page" when {
      "the vat number is unsuccessfully stored as the client is ineligible for mtd vat and one date is available" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStoreVatNumberIneligible(isFromBta = false, migratableDates = MigratableDates(Some(LocalDate.now())))

        val res = post("/client/confirm-vat-number", Map(SessionKeys.vatNumberKey -> testVatNumber))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.MigratableDatesController.show().url)
        )

        SessionCookieCrumbler.getSessionMap(res).get(SessionKeys.migratableDatesKey) shouldBe defined
      }
    }

    "redirect to the sign up after this date page" when {
      "the vat number is unsuccessfully stored as the client is ineligible for mtd vat and two dates is available" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStoreVatNumberIneligible(isFromBta = false, migratableDates = MigratableDates(Some(LocalDate.now()), Some(LocalDate.now())))

        val res = post("/client/confirm-vat-number", Map(SessionKeys.vatNumberKey -> testVatNumber))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.MigratableDatesController.show().url)
        )

        SessionCookieCrumbler.getSessionMap(res).get(SessionKeys.migratableDatesKey) shouldBe defined
      }
    }

    "redirect to the already signed up page" when {
      "the vat number has already been signed up" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStoreVatNumberAlreadySignedUp(isFromBta = false)

        val res = post("/client/confirm-vat-number", Map(SessionKeys.vatNumberKey -> testVatNumber))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.AlreadySignedUpController.show().url)
        )
      }
    }

    "redirect to the migration in progress error page" when {
      "the vat number has already been signed up and migration is in progress" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStoreVatNumberMigrationInProgress(isFromBta = false)

        val res = post("/client/confirm-vat-number", Map(SessionKeys.vatNumberKey -> testVatNumber))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.MigrationInProgressErrorController.show().url)
        )
      }
    }

    "throw an internal server error" when {
      "the vat number cannot be stored" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStoreVatNumberFailure(isFromBta = false)

        val res = post("/client/confirm-vat-number", Map(SessionKeys.vatNumberKey -> testVatNumber))()

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
  }

}
