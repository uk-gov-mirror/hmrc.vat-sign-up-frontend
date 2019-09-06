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

package uk.gov.hmrc.vatsignupfrontend.services

import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.DivisionLookupJourney
import uk.gov.hmrc.vatsignupfrontend.helpers.ComponentSpecBase

class AdministrativeDivisionLookupServiceISpec extends ComponentSpecBase {
  val administrativeDivisionVatNumber1 = "000000000"
  val administrativeDivisionVatNumber2 = "000000001"
  val nonAdministrativeDivisionVatNumber = "000000002"

  override val config: Map[String, String] = super.config + ("administrative-divisions" -> s"$administrativeDivisionVatNumber1,$administrativeDivisionVatNumber2)")
  val administrativeDivisionLookupService: AdministrativeDivisionLookupService = app.injector.instanceOf[AdministrativeDivisionLookupService]

  "isAdministrativeDivision" when {
    s"the $DivisionLookupJourney feature switch is enabled" when {
      "the VAT number is in the administrative division config list" should {
        "return true" in {
          enable(DivisionLookupJourney)

          administrativeDivisionLookupService.isAdministrativeDivision(administrativeDivisionVatNumber1) shouldBe true
        }
      }
      "the VAT number is not in the administrative division config list" should {
        "return false" in {
          enable(DivisionLookupJourney)

          administrativeDivisionLookupService.isAdministrativeDivision(nonAdministrativeDivisionVatNumber) shouldBe false
        }
      }
    }
    s"the $DivisionLookupJourney feature switch is disabled" when {
        "the VAT number is not in the administrative division config list" should {
          "return false" in {
            disable(DivisionLookupJourney)

          administrativeDivisionLookupService.isAdministrativeDivision(administrativeDivisionVatNumber1) shouldBe false
        }
      }
    }
  }

}
