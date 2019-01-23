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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.bta

import java.time.LocalDate

import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.BTAClaimSubscription
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.forms.BusinessPostCodeForm
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.ClaimSubscriptionStub.stubClaimSubscription
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.DateModel

class BtaBusinessPostCodeControllerISpec extends ComponentSpecBase with CustomMatchers {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(BTAClaimSubscription)
  }

  val testDate: DateModel = DateModel.dateConvert(LocalDate.now())

  "GET /bta/business-postcode" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/bta/business-postcode")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "if feature switch is disabled" should {
    "return a not found" in {
      disable(BTAClaimSubscription)

      val res = get("/bta/business-postcode")

      res should have(
        httpStatus(NOT_FOUND)
      )
    }
  }

  "the VAT subscription has been claimed" should {
    "redirect to sign up complete client" in {
      stubAuth(OK, successfulAuthResponse())
      stubClaimSubscription(testVatNumber, testBusinessPostCode, testDate, isFromBta = true)(NO_CONTENT)

      val res = post("/bta/business-postcode",
        Map(
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.vatRegistrationDateKey -> Json.toJson(testDate).toString()
        )
      )(BusinessPostCodeForm.businessPostCode -> testBusinessPostCode.postCode)

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(principalRoutes.SignUpCompleteClientController.show().url)
      )
    }
  }
  "Claim Subscription Service returned known facts mismatch" should {
    "redirect to could not confirm business" in {
      stubAuth(OK, successfulAuthResponse())
      stubClaimSubscription(testVatNumber, testBusinessPostCode, testDate, isFromBta = true)(FORBIDDEN)

      val res = post("/bta/business-postcode",
        Map(
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.vatRegistrationDateKey -> Json.toJson(testDate).toString()
        )
      )(BusinessPostCodeForm.businessPostCode -> testBusinessPostCode.postCode)

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.CouldNotConfirmBusinessController.show().url)
      )
    }
  }
  "Claim Subscription Service returned AlreadyEnrolledOnAnotherCredential" should {
    "redirect to business already signed up page" in {
      stubAuth(OK, successfulAuthResponse())
      stubClaimSubscription(testVatNumber, testBusinessPostCode, testDate, isFromBta = true)(CONFLICT)

      val res = post("/bta/business-postcode",
        Map(
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.vatRegistrationDateKey -> Json.toJson(testDate).toString()
        )
      )(BusinessPostCodeForm.businessPostCode -> testBusinessPostCode.postCode)

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.BusinessAlreadySignedUpController.show().url)
      )
    }
  }
}
