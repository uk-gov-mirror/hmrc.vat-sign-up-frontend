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

import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.YesNoMapping._
import uk.gov.hmrc.vatsignupfrontend.models.YesNo

object DoYouHaveAUtrForm {

  val yesNo: String = "yes_no"

  def doYouHaveAUtrForm(isAgent: Boolean): Form[YesNo] = Form(
    single(
      yesNo -> of(yesNoMapping(
        error =
          if (isAgent)
            "error.agent.partnership.do_you_have_a_utr"
          else
            "error.principal.partnership.do_you_have_a_utr"
      ))
    )
  )

}
