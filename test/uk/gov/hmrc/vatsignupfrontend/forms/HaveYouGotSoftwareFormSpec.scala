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

import play.api.data.FormError
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.forms.HaveYouGotSoftwareForm._
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.HaveSoftwareMapping._
import uk.gov.hmrc.vatsignupfrontend.models._

class HaveYouGotSoftwareFormSpec extends UnitSpec {

  val error: String = "error.principal.no_option_selected"

  "HaveYouGotSoftware Form" should {
    "successfully parse an AccountingSoftware" in {
      val result = haveYouGotSoftwareForm.bind(Map(software -> option_accounting_software))
      result.value should contain (AccountingSoftware)
    }
    "successfully parse a Spreadsheets" in {
      val result = haveYouGotSoftwareForm.bind(Map(software -> option_spreadsheets))
      result.value should contain (Spreadsheets)
    }
    "successfully parse an Neither" in {
      val result = haveYouGotSoftwareForm.bind(Map(software -> option_neither))
      result.value should contain (Neither)
    }
    "fail when nothing has been entered" in {
      val result = haveYouGotSoftwareForm.bind(Map.empty[String, String])
      result.errors should contain(FormError(software, error))
    }
  }
}
