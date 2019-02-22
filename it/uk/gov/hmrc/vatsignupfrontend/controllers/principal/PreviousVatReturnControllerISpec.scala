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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.AdditionalKnownFacts
import uk.gov.hmrc.vatsignupfrontend.forms.PreviousVatReturnForm
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class PreviousVatReturnControllerISpec extends ComponentSpecBase with CustomMatchers {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(AdditionalKnownFacts)
  }

  "GET /submitted-vat-return" when {
    "the feature switch is enabled" should {
      "return an OK" in {
        stubAuth(OK, successfulAuthResponse())

        val res = get("/submitted-vat-return")

        res should have(
          httpStatus(OK)
        )
      }

      "the feature switch is disabled" should {
        "return a not found" in {
          disable(AdditionalKnownFacts)

          val res = get("/submitted-vat-return")

          res should have(
            httpStatus(NOT_FOUND)
          )
        }
      }
    }
  }

  "POST /submitted-vat-return" should {
    "return a redirect to vat number" when {
      "form value is YES" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/submitted-vat-return")(PreviousVatReturnForm.yesNo -> YesNoMapping.option_yes)

        res should have (httpStatus(NOT_IMPLEMENTED))
      }
    }

    "redirect to check your answers page" when {
      "form value is NO" in {
        stubAuth(OK, successfulAuthResponse())


        val res = post("/submitted-vat-return")(PreviousVatReturnForm.yesNo -> YesNoMapping.option_no)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersController.show().url)
        )
      }
    }
  }

}
