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

import java.time.LocalDate

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys._
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.ReSignUpJourney
import uk.gov.hmrc.vatsignupfrontend.forms.MultipleVatCheckForm
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.ClaimSubscriptionStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreVatNumberStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.VatNumberEligibilityStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers, SessionCookieCrumbler}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.VatNumberEligibilityHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.MigratableDates

class MultipleVatCheckControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /more-than-one-vat-business" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/more-than-one-vat-business")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /more-than-one-vat-business" when {
    "the resignup feature switch is on" should {
      "return a redirect to vat number" when {
        "form value is YES" in {
          enable(ReSignUpJourney)
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))

          val res = post("/more-than-one-vat-business")(MultipleVatCheckForm.yesNo -> YesNoMapping.option_yes)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CaptureVatNumberController.show().url)
          )
        }
      }

      "return a redirect to business type with isMigrated in session" when {
        "form value is NO and the VRN is migrated" in {
          enable(ReSignUpJourney)
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubMigratedVatNumberEligibilitySuccess(testVatNumber)(EligibleValue, None, Some(Eligible(isOverseas = false, isMigrated = true)))
          stubStoreMigratedVatNumberSuccess

          val res = post("/more-than-one-vat-business")(MultipleVatCheckForm.yesNo -> YesNoMapping.option_no)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CaptureBusinessEntityController.show().url)
          )

          val session = SessionCookieCrumbler.getSessionMap(res)
          session.get(isMigratedKey) shouldBe Some("true")
        }
      }

      "return a redirect to business type with isDirectDebit in session" when {
        "form value is NO and the VRN is not migrated and does not have direct debit when there is only vat dec enrolment" in {
          enable(ReSignUpJourney)
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubMigratedVatNumberEligibilitySuccess(testVatNumber)(EligibleValue, None, Some(Eligible(isOverseas = false, isMigrated = false)))
          stubStoreVatNumberSuccess(isFromBta = false, isDirectDebit = false)

          val res = post("/more-than-one-vat-business")(MultipleVatCheckForm.yesNo -> YesNoMapping.option_no)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CaptureBusinessEntityController.show().url)
          )

          val session = SessionCookieCrumbler.getSessionMap(res)
          session.get(hasDirectDebitKey) shouldBe Some("false")
        }
      }

      "return a redirect to business type with isDirectDebit in session" when {
        "form value is NO and the VRN is not migrated and does not have direct debit when there is only mtd vat enrolment" in {
          enable(ReSignUpJourney)
          stubAuth(OK, successfulAuthResponse(mtdVatEnrolment))
          stubMigratedVatNumberEligibilitySuccess(testVatNumber)(EligibleValue, None, Some(Eligible(isOverseas = false, isMigrated = false)))
          stubStoreVatNumberSuccess(isFromBta = false, isDirectDebit = false)

          val res = post("/more-than-one-vat-business")(MultipleVatCheckForm.yesNo -> YesNoMapping.option_no)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CaptureBusinessEntityController.show().url)
          )

          val session = SessionCookieCrumbler.getSessionMap(res)
          session.get(hasDirectDebitKey) shouldBe Some("false")
        }
      }

      "return a redirect to business type with isDirectDebit in session" when {
        "form value is NO and the VRN is not migrated and does not have direct debit when both enrolments exist" in {
          enable(ReSignUpJourney)
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment, mtdVatEnrolment))
          stubMigratedVatNumberEligibilitySuccess(testVatNumber)(EligibleValue, None, Some(Eligible(isOverseas = false, isMigrated = false)))
          stubStoreVatNumberSuccess(isFromBta = false, isDirectDebit = false)

          val res = post("/more-than-one-vat-business")(MultipleVatCheckForm.yesNo -> YesNoMapping.option_no)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CaptureBusinessEntityController.show().url)
          )

          val session = SessionCookieCrumbler.getSessionMap(res)
          session.get(hasDirectDebitKey) shouldBe Some("false")
        }
      }

      "return a redirect to overseas resolver" when {
        "form value is NO and the VRN is not migrated and is overseas" in {
          enable(ReSignUpJourney)
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubMigratedVatNumberEligibilitySuccess(testVatNumber)(EligibleValue, None, Some(Eligible(isOverseas = true, isMigrated = false)))
          stubStoreVatNumberSuccess(isFromBta = false, isOverseasTrader = true)

          val res = post("/more-than-one-vat-business")(MultipleVatCheckForm.yesNo -> YesNoMapping.option_no)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.OverseasResolverController.resolve().url)
          )
        }
      }

      "return a redirect to migratable dates page" when {
        "form value is NO and the VRN is inhibited" in {
          enable(ReSignUpJourney)
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubMigratedVatNumberEligibilitySuccess(testVatNumber)(
            InhibitedValue, Some(MigratableDates(Some(testStartDate), Some(testEndDate))), None
          )

          val res = post("/more-than-one-vat-business")(MultipleVatCheckForm.yesNo -> YesNoMapping.option_no)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.MigratableDatesController.show().url)
          )
        }
      }

      "return a redirect to cannot use service error page" when {
        "form value is NO and the VRN is ineligible" in {
          enable(ReSignUpJourney)
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubMigratedVatNumberEligibilitySuccess(testVatNumber)(IneligibleValue, None, None)

          val res = post("/more-than-one-vat-business")(MultipleVatCheckForm.yesNo -> YesNoMapping.option_no)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CannotUseServiceController.show().url)
          )
        }
      }

      "redirect to migration in progress error page" when {
        "form value is NO and the VRN is currently being migrated" in {
          enable(ReSignUpJourney)
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubMigratedVatNumberEligibilitySuccess(testVatNumber)(MigrationInProgressValue, None, None)

          val res = post("/more-than-one-vat-business")(MultipleVatCheckForm.yesNo -> YesNoMapping.option_no)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.MigrationInProgressErrorController.show().url)
          )
        }
      }

      "redirect to business already signed up error page" when {
        "form value is NO and the vrn is already subscribed with the subscription successfully claimed" in {
          enable(ReSignUpJourney)
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubMigratedVatNumberEligibilitySuccess(testVatNumber)(AlreadySubscribedValue, None, None)
          stubClaimSubscription(testVatNumber, isFromBta = false)(NO_CONTENT)

          val res = post("/more-than-one-vat-business")(MultipleVatCheckForm.yesNo -> YesNoMapping.option_no)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.SignUpCompleteClientController.show().url)
          )
        }
      }

      "redirect to business already signed up error page" when {
        "form value is NO and the vrn is already enrolled on a different cred" in {
          enable(ReSignUpJourney)
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubMigratedVatNumberEligibilitySuccess(testVatNumber)(AlreadySubscribedValue, None, None)
          stubClaimSubscription(testVatNumber, isFromBta = false)(CONFLICT)

          val res = post("/more-than-one-vat-business")(MultipleVatCheckForm.yesNo -> YesNoMapping.option_no)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(bta.routes.BusinessAlreadySignedUpController.show().url)
          )
        }
      }

      "redirect to business already signed up error page" when {
        "form value is NO and the vrn is already signed up" in {
          enable(ReSignUpJourney)
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment, mtdVatEnrolment))
          stubMigratedVatNumberEligibilitySuccess(testVatNumber)(AlreadySubscribedValue, None, None)

          val res = post("/more-than-one-vat-business")(MultipleVatCheckForm.yesNo -> YesNoMapping.option_no)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.AlreadySignedUpController.show().url)
          )
        }
      }
    }

    "the resignup feature switch is off" should {
      "return a redirect to vat number" when {
        "form value is YES" in {
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))

          val res = post("/more-than-one-vat-business")(MultipleVatCheckForm.yesNo -> YesNoMapping.option_yes)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CaptureVatNumberController.show().url)
          )
        }
      }

      "return a redirect to business type" when {
        "form value is NO" in {
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubStoreVatNumberSuccess(isFromBta = false)

          val res = post("/more-than-one-vat-business")(MultipleVatCheckForm.yesNo -> YesNoMapping.option_no)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CaptureBusinessEntityController.show().url)
          )
        }
      }

      "return a redirect to overseas resolver" when {
        "form value is NO" in {
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubStoreVatNumberSuccess(isFromBta = false, isOverseasTrader = true)

          val res = post("/more-than-one-vat-business")(MultipleVatCheckForm.yesNo -> YesNoMapping.option_no)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.OverseasResolverController.resolve().url)
          )
        }
      }

      "return a redirect to migratable dates page" when {
        "form value is NO" in {
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubStoreVatNumberIneligible(isFromBta = false,
            migratableDates = MigratableDates(Some(LocalDate.now())))

          val res = post("/more-than-one-vat-business")(MultipleVatCheckForm.yesNo -> YesNoMapping.option_no)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.MigratableDatesController.show().url)
          )
        }
      }

      "return a redirect to cannot use service error page" when {
        "form value is NO" in {
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubStoreVatNumberIneligible(isFromBta = false,
            migratableDates = MigratableDates())

          val res = post("/more-than-one-vat-business")(MultipleVatCheckForm.yesNo -> YesNoMapping.option_no)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CannotUseServiceController.show().url)
          )
        }
      }

      "redirect to migration in progress error page" when {
        "form value is NO" in {
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubStoreVatNumberMigrationInProgress(isFromBta = false)

          val res = post("/more-than-one-vat-business")(MultipleVatCheckForm.yesNo -> YesNoMapping.option_no)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.MigrationInProgressErrorController.show().url)
          )
        }
      }

      "redirect to business already signed up error page" when {
        "form value is NO" in {
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubStoreVatNumberAlreadySignedUp(isFromBta = false)
          stubClaimSubscription(testVatNumber, isFromBta = false)(CONFLICT)

          val res = post("/more-than-one-vat-business")(MultipleVatCheckForm.yesNo -> YesNoMapping.option_no)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(bta.routes.BusinessAlreadySignedUpController.show().url)
          )
        }
      }

      "redirect to already signed up page" when {
        "form value is NO" in {
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment, mtdVatEnrolment))
          stubStoreVatNumberAlreadySignedUp(isFromBta = false)

          val res = post("/more-than-one-vat-business")(MultipleVatCheckForm.yesNo -> YesNoMapping.option_no)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.AlreadySignedUpController.show().url)
          )
        }
      }
    }
  }
}
