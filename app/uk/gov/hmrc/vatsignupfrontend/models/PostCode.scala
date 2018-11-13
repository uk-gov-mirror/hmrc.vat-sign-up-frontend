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

package uk.gov.hmrc.vatsignupfrontend.models

import play.api.libs.json.{Json, OFormat}


case class PostCode (postCode: String) {
  import PostCode._

  val sanitisedPostCode: String = postCode.toUpperCase filterNot(_.isWhitespace)

  lazy val checkYourAnswersFormat: String = postCode.replaceAll(" ", "").toUpperCase() match {
    case standardFormat(p1, p2) => p1 + " " + p2
    case bfpoFormat(p1, p2) => p1 + " " + p2
    case otherFormat => otherFormat // should never happen, since postcode should have passed validation before being put in this object
  }

  override def equals(that: Any): Boolean = that match {
    case otherPostCode: PostCode =>
      otherPostCode.sanitisedPostCode == sanitisedPostCode
    case _ =>
      false
  }

  override def hashCode(): Int = {
    val prime = 31
    val sanitisedPostCodeHashCode = if(sanitisedPostCode == null) 0 else sanitisedPostCode.hashCode
    prime * sanitisedPostCodeHashCode
  }
}

object PostCode {
  private[models] val standardFormat = "([A-Z]{1,2}[0-9][0-9A-Z]?)([0-9][A-Z]{2})".r
  private[models] val bfpoFormat = "(BFPO)([0-9]{1,3})".r

  implicit val format: OFormat[PostCode] = Json.format[PostCode]
}