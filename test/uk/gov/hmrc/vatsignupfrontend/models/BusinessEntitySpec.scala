/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.models

import play.api.libs.json.{JsNumber, JsString, JsValue}
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec

class BusinessEntitySpec extends UnitSpec {

  "BusinessEntityJsonReads" should {
    Map(
      BusinessEntity.LimitedCompanyKey -> LimitedCompany,
      BusinessEntity.SoleTraderKey -> SoleTrader,
      BusinessEntity.GeneralPartnershipKey -> GeneralPartnership,
      BusinessEntity.LimitedPartnershipKey -> LimitedPartnership,
      BusinessEntity.LimitedLiabilityPartnershipKey -> LimitedLiabilityPartnership,
      BusinessEntity.ScottishLimitedPartnershipKey -> ScottishLimitedPartnership,
      BusinessEntity.VatGroupKey -> VatGroup,
      BusinessEntity.DivisionKey -> Division,
      BusinessEntity.UnincorporatedAssociationKey -> UnincorporatedAssociation,
      BusinessEntity.TrustKey -> Trust,
      BusinessEntity.RegisteredSocietyKey -> RegisteredSociety,
      BusinessEntity.CharityKey -> Charity,
      BusinessEntity.OverseasKey -> Overseas,
      BusinessEntity.GovernmentOrganisationKey -> GovernmentOrganisation,
      BusinessEntity.OtherKey -> Other
    ).foreach { mapElem =>
      val (businessEntityKey, businessEntityType) = mapElem

      s"return jsSuccess and parse successfully to a BusinessEntity when value is $businessEntityKey" in {
        val json: JsValue = JsString(businessEntityKey)
        json.as[BusinessEntity](BusinessEntity.jsonReads) shouldBe businessEntityType
      }
    }

      "return a exception when type is not a string" in {
        val json: JsValue = JsNumber(123)
        intercept[Exception](json.as[BusinessEntity](BusinessEntity.jsonReads))
      }
      "return a exception when string is not a BusinessEntity constant" in {
        val json: JsValue = JsString("foo bar wizz")
     json.validate[BusinessEntity](BusinessEntity.jsonReads).asEither.left.get.head._2.head.message shouldBe "Is not a valid BusinessEntity"
      }
    }

}
