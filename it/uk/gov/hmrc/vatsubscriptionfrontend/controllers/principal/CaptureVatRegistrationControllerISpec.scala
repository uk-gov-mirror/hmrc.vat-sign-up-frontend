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

package uk.gov.hmrc.vatsubscriptionfrontend.controllers.principal

import java.time.LocalDate

import play.api.http.Status._
import uk.gov.hmrc.vatsubscriptionfrontend.config.featureswitch.KnownFactsJourney
import uk.gov.hmrc.vatsubscriptionfrontend.forms.VatRegistrationDateForm._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsubscriptionfrontend.models.DateModel

class CaptureVatRegistrationControllerISpec extends ComponentSpecBase with CustomMatchers {

  override def beforeEach(): Unit = enable(KnownFactsJourney)

  override def afterEach(): Unit = disable(KnownFactsJourney)

  "GET /vat-registration-date" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/vat-registration-date")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /vat-registration-date" should {
    "return a redirect" in {
      stubAuth(OK, successfulAuthResponse())

      val yesterday = DateModel.dateConvert(LocalDate.now().minusDays(1))

      val res = post("/vat-registration-date")(vatRegistrationDate + ".dateDay" -> yesterday.day,
                                                 vatRegistrationDate + ".dateMonth" -> yesterday.month,
                                                 vatRegistrationDate + ".dateYear" -> yesterday.year)

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.BusinessPostCodeController.show().url)
      )
    }
  }

  "Making a request to /vat-registration-date when not enabled" should {
    "return NotFound" in {
      disable(KnownFactsJourney)
      stubAuth(OK, successfulAuthResponse())

      val res = get("/vat-registration-date")

      res should have(
        httpStatus(NOT_FOUND)
      )

    }
  }
}
