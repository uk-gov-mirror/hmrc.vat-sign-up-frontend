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

package uk.gov.hmrc.vatsignupfrontend.services

import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import org.mockito.Mockito._
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._

class AdministrativeDivisionLookupServiceSpec extends UnitSpec with MockitoSugar with FeatureSwitching {

  class Setup {
    val mockAppConfig: AppConfig = mock[AppConfig]

    object TestAdministrativeDivisionLookupService extends AdministrativeDivisionLookupService(mockAppConfig)

  }

  "isAdministrativeDivision" when {
    "the vat number is in the list of administrative divisions" should {
      "return true" in new Setup {
        when(mockAppConfig.administrativeDivisionList).thenReturn(Set(testVatNumber))

        TestAdministrativeDivisionLookupService.isAdministrativeDivision(testVatNumber) shouldBe true
      }
    }
    "the vat number is not in the list of administrative divisions" in new Setup {
      when(mockAppConfig.administrativeDivisionList).thenReturn(Set.empty[String])

      TestAdministrativeDivisionLookupService.isAdministrativeDivision(testVatNumber) shouldBe false
    }
  }
}

