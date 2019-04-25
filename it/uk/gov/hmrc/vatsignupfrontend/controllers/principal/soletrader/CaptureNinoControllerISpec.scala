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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.soletrader

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.forms.NinoForm._
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{FeatureSwitching, SkipCidCheck}
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants.testNino

class CaptureNinoControllerISpec extends ComponentSpecBase with CustomMatchers with FeatureSwitching {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(SkipCidCheck)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    disable(SkipCidCheck)
  }

  val uri = "/national-insurance-number"

  "GET /national-insurance-number" when {
    "the SkipCidCheck feature switch is enabled" should {
      "return OK" in {
        stubAuth(OK, successfulAuthResponse())

        val res = get(uri)

        res should have {
          httpStatus(OK)
        }
      }
    }

    "the SkipCidCheck feature switch is disabled" should {
      "return NOT FOUND" in {
        disable(SkipCidCheck)
        stubAuth(OK, successfulAuthResponse())

        val res = get(uri)

        res should have {
          httpStatus(NOT_FOUND)
        }
      }
    }
  }

  "POST /national-insurance-number" when {
    "the NINO is valid" should {
      "redirect to confirm details" in {
        val res = post(uri)(nino -> testNino)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(principalRoutes.ConfirmYourDetailsController.show().url)
        )
      }
    }

    "the NINO is invalid" should {
      "return BAD REQUEST" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post(uri)(nino -> testNino.replace(testNino.substring(0, 1), "QQ"))

        res should have(
          httpStatus(BAD_REQUEST)
        )
      }
    }
  }

}
