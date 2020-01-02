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
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.forms.DoYouHaveAUtrForm._
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.YesNoMapping._
import uk.gov.hmrc.vatsignupfrontend.models.{No, Yes}

class DoYouHaveAUtrFormSpec extends UnitSpec {

  val principalError = "error.principal.partnership.do_you_have_a_utr"
  val agentError = "error.agent.partnership.do_you_have_a_utr"

  "YesNoForm" should {
    "successfully parse a Yes" in {
      val res = doYouHaveAUtrForm(isAgent = false).bind(Map(yesNo -> option_yes))
      res.value should contain(Yes)
    }

    "successfully parse a No" in {
      val res = doYouHaveAUtrForm(isAgent = false).bind(Map(yesNo -> option_no))
      res.value should contain(No)
    }

    "fail when nothing has been entered - principal flow" in {
      val res = doYouHaveAUtrForm(isAgent = false).bind(Map.empty[String, String])
      res.errors should contain(FormError(yesNo, principalError))
    }

    "fail when nothing has been entered - agent flow" in {
      val res = doYouHaveAUtrForm(isAgent = true).bind(Map.empty[String, String])
      res.errors should contain(FormError(yesNo, agentError))
    }
  }

}
