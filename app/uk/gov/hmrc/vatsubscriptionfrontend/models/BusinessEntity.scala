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

package uk.gov.hmrc.vatsubscriptionfrontend.models

import uk.gov.hmrc.vatsubscriptionfrontend.utils.SessionUtils.SessionFormatter

sealed trait BusinessEntity

object LimitedCompany extends BusinessEntity

object SoleTrader extends BusinessEntity

object Other extends BusinessEntity

object BusinessEntity {
  val LimitedCompanyKey = "limited-company"
  val SoleTraderKey = "sole-trader"
  val OtherKey = "other"

  implicit object BusinessEntitySessionFormatter extends SessionFormatter[BusinessEntity] {
    override def fromString(string: String): Option[BusinessEntity] = string match {
      case LimitedCompanyKey => Some(LimitedCompany)
      case SoleTraderKey => Some(SoleTrader)
      case OtherKey => Some(Other)
      case _ => None
    }

    override def toString(entity: BusinessEntity): String = entity match {
      case LimitedCompany => LimitedCompanyKey
      case SoleTrader => SoleTraderKey
    }
  }
}
