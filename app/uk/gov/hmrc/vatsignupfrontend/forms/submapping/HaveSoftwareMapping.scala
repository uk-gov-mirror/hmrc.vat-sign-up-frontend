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

package uk.gov.hmrc.vatsignupfrontend.forms.submapping

import play.api.data.FormError
import play.api.data.format.Formatter
import uk.gov.hmrc.vatsignupfrontend.models._

object HaveSoftwareMapping {

  val option_accounting_software: String = "I use accounting software"
  val option_spreadsheets: String = "I use spreadsheets"
  val option_neither: String = "I use neither"

  def haveSoftwareMapping(error: String): Formatter[HaveSoftware] = new Formatter[HaveSoftware] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], HaveSoftware] = {
      data.get(key) match {
        case Some(`option_accounting_software`) => Right(AccountingSoftware)
        case Some(`option_spreadsheets`) => Right(Spreadsheets)
        case Some(`option_neither`) => Right(Neither)
        case _ => Left(Seq(FormError(key, error)))
      }
    }

    override def unbind(key: String, value: HaveSoftware): Map[String, String] = {
      val stringValue = value match {
        case AccountingSoftware => option_accounting_software
        case Spreadsheets => option_spreadsheets
        case Neither => option_neither
      }
      Map(key->stringValue)
    }
  }
}
