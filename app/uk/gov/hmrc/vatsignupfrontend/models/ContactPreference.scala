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

package uk.gov.hmrc.vatsignupfrontend.models

import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils.SessionFormatter

sealed trait ContactPreference

case object Paper extends ContactPreference

case object Digital extends ContactPreference

case object ContactPreference {
  val PaperKey = "Paper"
  val DigitalKey = "Digital"

  implicit val contactPreferenceFormat: SessionFormatter[ContactPreference] = new SessionFormatter[ContactPreference] {
    override def toString(contactPreference: ContactPreference): String =
      contactPreference match {
        case Paper => PaperKey
        case Digital => DigitalKey
      }

    override def fromString(string: String): Option[ContactPreference] =
      string match {
        case PaperKey => Some(Paper)
        case DigitalKey => Some(Digital)
        case _ => None
      }
  }
}
