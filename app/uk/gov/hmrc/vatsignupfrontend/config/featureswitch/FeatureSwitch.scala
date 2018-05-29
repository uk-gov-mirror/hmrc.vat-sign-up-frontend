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

package uk.gov.hmrc.vatsignupfrontend.config.featureswitch

import FeatureSwitch.prefix

sealed trait FeatureSwitch {
  val name: String
  val displayText: String
}

object FeatureSwitch {
  val prefix = "feature-switch"

  val switches: Set[FeatureSwitch] = Set(
    KnownFactsJourney,
    CompanyNameJourney,
    StubIncorporationInformation
  )

  def apply(str: String): FeatureSwitch =
    switches find (_.name == str) match {
      case Some(switch) => switch
      case None => throw new IllegalArgumentException("Invalid feature switch: " + str)
    }

  def get(str: String): Option[FeatureSwitch] = switches find (_.name == str)

}

case object KnownFactsJourney extends FeatureSwitch {
  override val name: String = s"$prefix.known-facts-journey"
  override val displayText: String = "Enable known facts confirmation of VAT number for principal users"
}

case object CompanyNameJourney extends FeatureSwitch {
  override val name: String = s"$prefix.company-name-journey"
  override val displayText: String = "Enable company name confirmation for principal users"
}

case object StubIncorporationInformation extends FeatureSwitch {
  override val name: String = s"$prefix.stub-incorporation-information"
  override val displayText: String = "Use Stub for Incorporation Information Connection"
}

case object UseIRSA extends FeatureSwitch {
  override val name: String = s"$prefix.use-ir-sa"
  override val displayText: String = "Use UTR from IR-SA to retrieve NINO"
}
