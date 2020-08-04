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

import java.time.LocalDate

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.forms.BusinessPostCodeForm
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.DateModel

class BusinessPostCodeControllerISpec extends ComponentSpecBase with CustomMatchers {

  val testDate: DateModel = DateModel.dateConvert(LocalDate.now())

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
      "the session contains isAlreadySubscribed: true" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/business-postcode", Map(SessionKeys.isAlreadySubscribedKey -> "true"))(
          BusinessPostCodeForm.businessPostCode -> testBusinessPostCode.postCode
        )

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersController.show().url)
        )
      }

      "the session contains isMigrated: true" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/business-postcode", Map(SessionKeys.isMigratedKey -> "true"))(
          BusinessPostCodeForm.businessPostCode -> testBusinessPostCode.postCode
        )

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersController.show().url)
        )
      }

    }

    "redirect to the previous vat return page" when {
      "the session does not contain isMigrated: true" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/business-postcode")(BusinessPostCodeForm.businessPostCode -> testBusinessPostCode.postCode)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.PreviousVatReturnController.show().url)
        )
      }
    }
  }
}
