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

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.FormError
import uk.gov.hmrc.vatsignupfrontend.forms.NinoForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants.testNino

class NinoFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  "the NINO form" should {

    val validateNinoForm = ninoForm(isAgent = false)

    val notEnteredPrincipalErrorKey = "error.principal.no_entry_nino"
    val notEnteredAgentErrorKey = "error.agent.no_entry_nino"
    val invalidNinoKey = "error.invalid_nino"
    val invalidLengthKey = "error.character_limit_nino"

    "Ensure a valid NINO passes validation" in {
      val validNino = testNino
      val value = validateNinoForm.bind(Map(nino -> validNino)).value
      value shouldBe Some(validNino)
    }

    "should allow NINOs with spaces" in {
      val validNino = testNino
      val validNinoWithSpaces = validNino.grouped(2).mkString(" ")
      val value = validateNinoForm.bind(Map(nino -> validNinoWithSpaces)).value
      value shouldBe Some(validNino)
    }

    "validate that a NINO has been entered - agent" in {
      val validateNinoForm = ninoForm(isAgent = true)
      val formWithError = validateNinoForm.bind(Map(nino -> ""))
      formWithError.errors should contain(FormError(nino, notEnteredAgentErrorKey))
    }

    "validate that a NINO has been entered - principal" in {
      val formWithError = validateNinoForm.bind(Map(nino -> ""))
      formWithError.errors should contain(FormError(nino, notEnteredPrincipalErrorKey))
    }

    "Validate that invalid prefixes are not allowed" in {
      val formWithError = validateNinoForm.bind(Map(nino -> "QQ123456C"))
      formWithError.errors should contain(FormError(nino, invalidNinoKey))
    }

    "validate that short NINOs are not allowed" in {
      val shortNino = "AB12345"
      val formWithError = validateNinoForm.bind(Map(nino -> shortNino))
      formWithError.errors should contain(FormError(nino, invalidLengthKey))
    }

  }

}
