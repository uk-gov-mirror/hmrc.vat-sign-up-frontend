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
import uk.gov.hmrc.vatsignupfrontend.forms.HaveSoftwareForm
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.YesNoMapping._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class HaveSoftwareControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /have-software" should {
    "return an OK" in {
      val res = get("/have-software")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /have-software" should {
    "return a redirect to vat number" when {
      "form value is YES" in {
        val res = post("/have-software")(HaveSoftwareForm.yesNo -> option_yes)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.SoftwareReadyController.show().url)
        )
      }
    }

    "return a redirect to business type" when {
      "form value is NO" in {
        val res = post("/have-software")(HaveSoftwareForm.yesNo -> option_no)

        res should have(
          httpStatus(SEE_OTHER)//,
//          redirectUri(routes.asdasdController.show().url)
        )
      }
    }
  }

}
