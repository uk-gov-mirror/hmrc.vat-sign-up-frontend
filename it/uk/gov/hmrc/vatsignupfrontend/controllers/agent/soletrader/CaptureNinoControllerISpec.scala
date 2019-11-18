/*
 * Copyright 2019 HM Revenue & Customs
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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.soletrader.routes.ConfirmNinoController
import uk.gov.hmrc.vatsignupfrontend.forms.NinoForm.nino
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants.testNino
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class CaptureNinoControllerISpec extends ComponentSpecBase with CustomMatchers with FeatureSwitching {

  val url = "/client/national-insurance-number"

  "GET /client/national-insurance-number" when {
    "return OK" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get(url)

      res should have {
        httpStatus(OK)
      }
    }
  }

  "POST /client/national-insurance-number" when {
    "the NINO is valid" should {
      "redirect to confirm nino" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = post(url)(nino -> testNino)

        res should have {
          httpStatus(SEE_OTHER)
          redirectUri(ConfirmNinoController.show().url)
        }
      }
    }
    "the NINO is not valid" should {
      "return BAD REQUEST" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = post(url)(nino -> testNino.replace(testNino.substring(0, 1), "QQ"))

        res should have {
          httpStatus(BAD_REQUEST)
        }
      }
    }
  }
}
