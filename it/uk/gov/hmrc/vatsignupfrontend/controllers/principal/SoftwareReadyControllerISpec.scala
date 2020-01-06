/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.vatsignupfrontend.forms.SoftwareReadyForm
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class SoftwareReadyControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /software-ready" should {
    "return an OK" in {

      val res = get("/software-ready")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /software-ready" should {
    "redirect to vat number resolver" when {
      "form value is YES" in {

        val res = post("/software-ready")(SoftwareReadyForm.yesNo -> YesNoMapping.option_yes)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.ResolveVatNumberController.resolve().url)
        )
      }
    }

    "redirect to verify software error page" when {
      "form value is NO" in {

        val res = post("/software-ready")(SoftwareReadyForm.yesNo -> YesNoMapping.option_no)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.VerifySoftwareErrorController.show().url)
        )
      }
    }
  }

}
