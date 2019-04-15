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

package uk.gov.hmrc.vatsignupfrontend.config.featureswitch

import FeatureSwitch.prefix

sealed trait FeatureSwitch {
  val name: String
  val displayText: String
}

object FeatureSwitch {
  val prefix = "feature-switch"

  val switches: Set[FeatureSwitch] = Set(
    StubIncorporationInformation,
    BTAClaimSubscription,
    DivisionJourney,
    UnincorporatedAssociationJourney,
    TrustJourney,
    RegisteredSocietyJourney,
    GovernmentOrganisationJourney,
    AdditionalKnownFacts,
    DirectDebitTermsJourney,
    ContactPreferencesJourney,
    SendYourApplication,
    OptionalSautrJourney,
    SkipIvJourney
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

case object UnplannedShutter extends FeatureSwitch {
  override val name: String = s"$prefix.unplanned-shutter"
  override val displayText: String = "Unplanned shutter for the service"
}

case object DivisionJourney extends FeatureSwitch {
  override val name: String = s"$prefix.division-journey"
  override val displayText: String = "Enable users to enter the division flow"
}

case object UnincorporatedAssociationJourney extends FeatureSwitch {
  override val name: String = s"$prefix.unincorporated-association-journey"
  override val displayText: String = "Enable users to enter unincorporated association flow"
}

case object TrustJourney extends FeatureSwitch {
  override val name: String = s"$prefix.trust-journey"
  override val displayText: String = "Enable users to enter trust flow"
}

case object RegisteredSocietyJourney extends FeatureSwitch {
  override val name: String = s"$prefix.registered-society-journey"
  override val displayText: String = "Enable users to enter registered society flow"
}

case object GovernmentOrganisationJourney extends FeatureSwitch {
  override val name: String = s"$prefix.government-organisation-journey"
  override val displayText: String = "Enable users to enter government organisation flow"
}

case object WelshTranslation extends FeatureSwitch {
  override val name: String = s"$prefix.welsh-translation"
  override val displayText: String = "Enable welsh language"
}

case object AdditionalKnownFacts extends FeatureSwitch {
  override val name: String = s"$prefix.additional-known-facts"
  override val displayText: String = "Enable the two additional Known Facts"
}

case object DirectDebitTermsJourney extends FeatureSwitch {
  override val name: String = s"$prefix.direct-debit-terms-journey"
  override val displayText: String = "Enable the Direct Debit T&Cs Journey"
}

case object ContactPreferencesJourney extends FeatureSwitch {
  override val name: String = s"$prefix.contact-preferences-journey"
  override val displayText: String = "Enable the Contact Preferences Journey"
}

case object OptionalSautrJourney extends FeatureSwitch {
  override val name: String = s"$prefix.optional-sautr-journey"
  override val displayText: String = "Enable the 'Do you have an SAUTR' question for general partnerships"
}
case object SendYourApplication extends FeatureSwitch {
  override val name: String = s"$prefix.send-your-application"
  override val displayText: String = "Enable the Send Your Application view (replaces Terms of Participation)"
}

case object SkipIvJourney extends FeatureSwitch {
  override val name: String = s"$prefix.no-iv-journey"
  override val displayText: String = "Enable users to skip Identity Verification Journey"
}