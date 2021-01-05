/*
 * Copyright 2021 HM Revenue & Customs
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

object MonthMapping {

  val option_jan: String = "January"
  val option_feb: String = "February"
  val option_mar: String = "March"
  val option_apr: String = "April"
  val option_may: String = "May"
  val option_jun: String = "June"
  val option_jul: String = "July"
  val option_aug: String = "August"
  val option_sep: String = "September"
  val option_oct: String = "October"
  val option_nov: String = "November"
  val option_dec: String = "December"

  def monthMapping(error: String): Formatter[Month] = new Formatter[Month] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Month] = {
      data.get(key) match {
        case Some(`option_jan`) => Right(January)
        case Some(`option_feb`) => Right(February)
        case Some(`option_mar`) => Right(March)
        case Some(`option_apr`) => Right(April)
        case Some(`option_may`) => Right(May)
        case Some(`option_jun`) => Right(June)
        case Some(`option_jul`) => Right(July)
        case Some(`option_aug`) => Right(August)
        case Some(`option_sep`) => Right(September)
        case Some(`option_oct`) => Right(October)
        case Some(`option_nov`) => Right(November)
        case Some(`option_dec`) => Right(December)
        case _ => Left(Seq(FormError(key, error)))
      }
    }

    override def unbind(key: String, value: Month): Map[String, String] = {
      val stringValue = value match {
        case January => option_jan
        case February => option_feb
        case March => option_mar
        case April => option_apr
        case May => option_may
        case June => option_jun
        case July => option_jul
        case August => option_aug
        case September => option_sep
        case October => option_oct
        case November => option_nov
        case December => option_dec
      }

      Map(key -> stringValue)
    }
  }
}
