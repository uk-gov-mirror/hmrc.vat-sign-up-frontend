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

package uk.gov.hmrc.vatsignupfrontend.forms

import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import uk.gov.hmrc.vatsignupfrontend.models._

object BusinessEntityForm {

  val businessEntity: String = "business-entity"

  val soleTrader: String = "sole-trader"

  val limitedCompany: String = "limited-company"

  val generalPartnership: String = "general-partnership"

  val limitedPartnership: String = "limited-partnership"

  val other: String = "other"

  val agentBusinessEntityError: String = "error.agent.business-entity"

  val principalBusinessEntityError: String = "error.principal.business-entity"


  private def formatter(isAgent: Boolean): Formatter[BusinessEntity] = new Formatter[BusinessEntity] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BusinessEntity] = {
      data.get(key) match {
        case Some(`soleTrader`) => Right(SoleTrader)
        case Some(`limitedCompany`) => Right(LimitedCompany)
        case Some(`generalPartnership`) => Right(GeneralPartnership)
        case Some(`limitedPartnership`) => Right(LimitedPartnership)
        case Some(`other`) => Right(Other)
        case _ => Left(Seq(FormError(key, if (isAgent) agentBusinessEntityError else principalBusinessEntityError)))
      }
    }

    override def unbind(key: String, value: BusinessEntity): Map[String, String] = {
      val stringValue = value match {
        case SoleTrader => soleTrader
        case LimitedCompany => limitedCompany
        case Other => other
      }

      Map(key -> stringValue)
    }
  }

  def businessEntityForm(isAgent: Boolean): Form[BusinessEntity] = Form(
    single(
      businessEntity -> of(formatter(isAgent = isAgent))
    )
  )
}
