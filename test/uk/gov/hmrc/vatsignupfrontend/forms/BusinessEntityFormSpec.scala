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

package uk.gov.hmrc.vatsignupfrontend.forms

import play.api.data.FormError
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.forms.BusinessEntityForm._
import uk.gov.hmrc.vatsignupfrontend.models._

class BusinessEntityFormSpec extends UnitSpec {
  "businessEntityForm" should {

    val businessEntityErrorKey = "error.business-entity"

    "successfully parse a sole trader entity" in {
      val res = businessEntityForm.bind(Map(businessEntity -> soleTrader))
      res.value should contain(SoleTrader)
    }

    "successfully parse a limited company entity" in {
      val res = businessEntityForm.bind(Map(businessEntity -> limitedCompany))
      res.value should contain(LimitedCompany)
    }

    "successfully parse a general partnership entity" in {
      val res = businessEntityForm.bind(Map(businessEntity -> generalPartnership))
      res.value should contain(GeneralPartnership)
    }

    "successfully parse a limited partnership entity" in {
      val res = businessEntityForm.bind(Map(businessEntity -> limitedPartnership))
      res.value should contain(LimitedPartnership)
    }

    "successfully parse a other entity" in {
      val res = businessEntityForm.bind(Map(businessEntity -> other))
      res.value should contain(Other)
    }

    "fail when nothing has been entered in the view" in {
      val res = businessEntityForm.bind(Map.empty[String, String])
      res.errors should contain(FormError(businessEntity, businessEntityErrorKey))
    }

    "fail when it is not an expected value in the view" in {
      val res = businessEntityForm.bind(Map(businessEntity -> "invalid"))
      res.errors should contain(FormError(businessEntity, businessEntityErrorKey))
    }
  }
}
