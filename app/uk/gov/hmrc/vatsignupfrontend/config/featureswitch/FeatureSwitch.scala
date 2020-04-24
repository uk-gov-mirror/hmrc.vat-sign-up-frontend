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

package uk.gov.hmrc.vatsignupfrontend.config.featureswitch

import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.FeatureSwitch.prefix

sealed trait FeatureSwitch {
  val name: String
  val displayText: String
}

object FeatureSwitch {
  val prefix = "feature-switch"

  val switches: Set[FeatureSwitch] = Set(
    StubIncorporationInformation,
    BTAClaimSubscription,
    AdditionalKnownFacts,
    WelshTranslation,
    FinalCheckYourAnswer,
    GeneralPartnershipNoSAUTR,
    CrnDissolved
  )

  def apply(str: String): FeatureSwitch =
    switches find (_.name == str) match {
      case Some(switch) => switch
      case None => throw new IllegalArgumentException("Invalid feature switch: " + str)
    }

  def get(str: String): Option[FeatureSwitch] = switches find (_.name == str)

}

case object StubIncorporationInformation extends FeatureSwitch {
  override val name: String = s"$prefix.stub-incorporation-information"
  override val displayText: String = "Use Stub for Incorporation Information Connection"
}

case object BTAClaimSubscription extends FeatureSwitch {
  override val name: String = s"$prefix.bta-claim-subscription"
  override val displayText: String = "Enable users from BTA to claim their subscription through VAT sign up"
}

case object WelshTranslation extends FeatureSwitch {
  override val name: String = s"$prefix.welsh-translation"
  override val displayText: String = "Enable welsh language"
}

case object AdditionalKnownFacts extends FeatureSwitch {
  override val name: String = s"$prefix.additional-known-facts"
  override val displayText: String = "Enable the two additional Known Facts"
}

case object FinalCheckYourAnswer extends FeatureSwitch {
  override val name: String = s"$prefix.final-check-your-answer"
  override val displayText: String = "Enable users to view check your answers and declaration page for individual or agent "
}

case object GeneralPartnershipNoSAUTR extends FeatureSwitch {
  override val name: String = s"$prefix.General-partnership-no-sautr"
  override val displayText: String = "Enable users to view General Partnership No SAUTR page for individual or agent "
}

case object CrnDissolved extends FeatureSwitch {
  override val name: String = s"$prefix.crn-dissolved-validation"
  override val displayText: String = "Block users with CRNs for dissolved or converted-closed companies"
}
