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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.soletrader

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys.ninoKey
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.vatsignupfrontend.forms.NinoForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants.testNino
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers, SessionCookieCrumbler}

class CaptureNinoControllerISpec extends ComponentSpecBase with CustomMatchers with FeatureSwitching {

  val uri = "/national-insurance-number"

  "GET /national-insurance-number" when {
    "return OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get(uri)

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /national-insurance-number" when {
    "the NINO is valid" should {
      "redirect to confirm NINO with the NINO in session" in {
        stubAuth(OK, successfulAuthResponse())
        val res = post(uri)(nino -> testNino)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.ConfirmNinoController.show().url)
        )

        val session = SessionCookieCrumbler.getSessionMap(res)
        session.keys should contain(ninoKey)
      }
    }
  }
}
