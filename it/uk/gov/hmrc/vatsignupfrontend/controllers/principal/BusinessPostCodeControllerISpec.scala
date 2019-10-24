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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.AdditionalKnownFacts
import uk.gov.hmrc.vatsignupfrontend.forms.BusinessPostCodeForm
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class BusinessPostCodeControllerISpec extends ComponentSpecBase with CustomMatchers {
  "GET /business-postcode" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/business-postcode")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /business-postcode" should {
    "redirect to check your answers" when {
      "the session contains isMigrated: true and the Additional Known Facts feature switch is enabled" in {
        stubAuth(OK, successfulAuthResponse())
        enable(AdditionalKnownFacts)

        val res = post("/business-postcode", Map(SessionKeys.isMigratedKey -> "true"))(
          BusinessPostCodeForm.businessPostCode -> testBusinessPostCode.postCode
        )

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersController.show().url)
        )
      }
      "the session does not contain isMigrated: true in session and the Additional Known Facts feature switch is disabled" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/business-postcode")(BusinessPostCodeForm.businessPostCode -> testBusinessPostCode.postCode)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersController.show().url)
        )
      }
    }
    "redirect to the previous vat return page" when {
      "the session does not contain isMigrated: true and the Additional Known Facts feature switch is enabled" in {
        stubAuth(OK, successfulAuthResponse())
        enable(AdditionalKnownFacts)

        val res = post("/business-postcode")(BusinessPostCodeForm.businessPostCode -> testBusinessPostCode.postCode)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.PreviousVatReturnController.show().url)
        )
      }
    }
  }

}
