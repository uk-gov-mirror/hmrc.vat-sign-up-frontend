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

package uk.gov.hmrc.vatsubscriptionfrontend.forms

import play.api.data.{Form, FormError}
import play.api.data.Forms._
import play.api.data.format.Formatter
import uk.gov.hmrc.vatsubscriptionfrontend.models.{BusinessEntity, LimitedCompany, Other, SoleTrader}

object BusinessEntityForm {

  val businessEntity: String = "business-entity"

  val soleTrader: String = "sole-trader"

  val limitedCompany: String = "limited-company"

  val other: String = "other"

  val businessEntityError: String = "error.business-entity"

  private val formatter: Formatter[BusinessEntity] = new Formatter[BusinessEntity] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BusinessEntity] = {
      data.get(key) match {
        case Some(`soleTrader`) => Right(SoleTrader)
        case Some(`limitedCompany`) => Right(LimitedCompany)
        case Some(`other`) => Right(Other)
        case _ => Left(Seq(FormError(key, businessEntityError)))
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

  val businessEntityForm: Form[BusinessEntity] = Form(
    single(
      businessEntity -> of(formatter)
    )
  )
}
