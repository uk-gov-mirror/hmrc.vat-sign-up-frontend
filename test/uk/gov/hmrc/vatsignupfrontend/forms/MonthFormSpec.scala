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

package uk.gov.hmrc.vatsignupfrontend.forms

import play.api.data.FormError
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.forms.MonthForm._
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.MonthMapping._
import uk.gov.hmrc.vatsignupfrontend.models._

class MonthFormSpec extends UnitSpec {

  val error: String = "error.no_month_selected"

  "MonthForm" should {
    "successfully parse a January" in {
      val res = monthForm.bind(Map(month -> option_jan))
      res.value should contain(January)
    }
    "successfully parse a February" in {
      val res = monthForm.bind(Map(month -> option_feb))
      res.value should contain(February)
    }
    "successfully parse a March" in {
      val res = monthForm.bind(Map(month -> option_mar))
      res.value should contain(March)
    }
    "successfully parse a April" in {
      val res = monthForm.bind(Map(month -> option_apr))
      res.value should contain(April)
    }
    "successfully parse a May" in {
      val res = monthForm.bind(Map(month -> option_may))
      res.value should contain(May)
    }
    "successfully parse a June" in {
      val res = monthForm.bind(Map(month -> option_jun))
      res.value should contain(June)
    }
    "successfully parse a July" in {
      val res = monthForm.bind(Map(month -> option_jul))
      res.value should contain(July)
    }
    "successfully parse a August" in {
      val res = monthForm.bind(Map(month -> option_aug))
      res.value should contain(August)
    }
    "successfully parse a September" in {
      val res = monthForm.bind(Map(month -> option_sep))
      res.value should contain(September)
    }
    "successfully parse a October" in {
      val res = monthForm.bind(Map(month -> option_oct))
      res.value should contain(October)
    }
    "successfully parse a November" in {
      val res = monthForm.bind(Map(month -> option_nov))
      res.value should contain(November)
    }
    "successfully parse a December" in {
      val res = monthForm.bind(Map(month -> option_dec))
      res.value should contain(December)
    }

    "fail when nothing has been entered" in {
      val res = monthForm.bind(Map.empty[String, String])
      res.errors should contain(FormError(month, error))
    }
  }
}
