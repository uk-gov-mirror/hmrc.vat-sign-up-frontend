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

package uk.gov.hmrc.vatsignupfrontend.forms

import play.api.data.{Form, FormError}
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.forms.OtherBusinessEntityForm._
import uk.gov.hmrc.vatsignupfrontend.models._

class OtherBusinessEntityFormSpec extends UnitSpec {

  "businessEntityForm" should {

    val principalBusinessEntityErrorKey = "error.principal.business-entity-other"
    val agentBusinessEntityErrorKey = "error.agent.business-entity-other"

    val principalBusinessEntityForm: Form[BusinessEntity] = businessEntityForm(isAgent = false)
    val agentBusinessEntityForm: Form[BusinessEntity] = businessEntityForm(isAgent = true)

    "successfully parse a vat group entity" in {
      val res = principalBusinessEntityForm.bind(Map(businessEntity -> vatGroup))
      res.value should contain(VatGroup)
    }

    "successfully parse an unincorporated association entity" in {
      val res = principalBusinessEntityForm.bind(Map(businessEntity -> unincorporatedAssociation))
      res.value should contain(UnincorporatedAssociation)
    }

    "successfully parse a government organisation entity" in {
      val res = principalBusinessEntityForm.bind(Map(businessEntity -> governmentOrganisation))
      res.value should contain(GovernmentOrganisation)
    }

    "fail when nothing has been entered in the view - principal" in {
      val res = principalBusinessEntityForm.bind(Map.empty[String, String])
      res.errors should contain(FormError(businessEntity, principalBusinessEntityErrorKey))
    }

    "fail when nothing has been entered in the view - agent" in {
      val res = agentBusinessEntityForm.bind(Map.empty[String, String])
      res.errors should contain(FormError(businessEntity, agentBusinessEntityErrorKey))
    }

    "fail when it is not an expected value in the view - principal" in {
      val res = principalBusinessEntityForm.bind(Map(businessEntity -> "invalid"))
      res.errors should contain(FormError(businessEntity, principalBusinessEntityErrorKey))
    }

    "fail when it is not an expected value in the view - agent" in {
      val res = agentBusinessEntityForm.bind(Map(businessEntity -> "invalid"))
      res.errors should contain(FormError(businessEntity, agentBusinessEntityErrorKey))
    }
  }

}
