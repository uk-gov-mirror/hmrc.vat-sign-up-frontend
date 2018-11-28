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

package uk.gov.hmrc.vatsignupfrontend.forms

import play.api.data.FormError
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.forms.BusinessEntityForm._
import uk.gov.hmrc.vatsignupfrontend.models._

class BusinessEntityFormSpec extends UnitSpec {
  "businessEntityForm" should {

    val agentBusinessEntityErrorKey = "error.agent.business-entity"
    val principalBusinessEntityErrorKey = "error.principal.business-entity"
    val validateBusinessEntityForm = businessEntityForm(isAgent = false)

    "successfully parse a sole trader entity" in {
      val res = validateBusinessEntityForm.bind(Map(businessEntity -> soleTrader))
      res.value should contain(SoleTrader)
    }

    "successfully parse a limited company entity" in {
      val res = validateBusinessEntityForm.bind(Map(businessEntity -> limitedCompany))
      res.value should contain(LimitedCompany)
    }

    "successfully parse a general partnership entity" in {
      val res = validateBusinessEntityForm.bind(Map(businessEntity -> generalPartnership))
      res.value should contain(GeneralPartnership)
    }

    "successfully parse a limited partnership entity" in {
      val res = validateBusinessEntityForm.bind(Map(businessEntity -> limitedPartnership))
      res.value should contain(LimitedPartnership)
    }

    "successfully parse a vat group entity" in {
      val res = validateBusinessEntityForm.bind(Map(businessEntity -> vatGroup))
      res.value should contain(VatGroup)
    }

    "successfully parse a division entity" in {
      val res = validateBusinessEntityForm.bind(Map(businessEntity -> division))
      res.value should contain(Division)
    }

    "successfully parse a other entity" in {
      val res = validateBusinessEntityForm.bind(Map(businessEntity -> other))
      res.value should contain(Other)
    }

    "fail when nothing has been entered in agent view" in {
      val validateBusinessEntityForm = businessEntityForm(isAgent = true)
      val res = validateBusinessEntityForm.bind(Map.empty[String, String])
      res.errors should contain(FormError(businessEntity, agentBusinessEntityErrorKey))
    }

    "fail when nothing has been entered in principal view" in {
      val res = validateBusinessEntityForm.bind(Map.empty[String, String])
      res.errors should contain(FormError(businessEntity, principalBusinessEntityErrorKey))
    }

    "fail when it is not an expected value in the agent view" in {
      val validateBusinessEntityForm = businessEntityForm(isAgent = true)
      val res = validateBusinessEntityForm.bind(Map(businessEntity -> "invalid"))
      res.errors should contain(FormError(businessEntity, agentBusinessEntityErrorKey))
    }

    "fail when it is not an expected value in the principal view" in {
      val res = validateBusinessEntityForm.bind(Map(businessEntity -> "invalid"))
      res.errors should contain(FormError(businessEntity, principalBusinessEntityErrorKey))
    }
  }
}
