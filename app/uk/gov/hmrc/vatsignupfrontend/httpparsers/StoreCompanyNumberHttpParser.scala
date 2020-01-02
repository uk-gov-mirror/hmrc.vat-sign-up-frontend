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

package uk.gov.hmrc.vatsignupfrontend.httpparsers

import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

import scala.util.Try

object StoreCompanyNumberHttpParser {
  type StoreCompanyNumberResponse = Either[StoreCompanyNumberFailure, StoreCompanyNumberSuccess.type]

  implicit object StoreCompanyNumberHttpReads extends HttpReads[StoreCompanyNumberResponse] {
    override def read(method: String, url: String, response: HttpResponse): StoreCompanyNumberResponse = {
      lazy val isCtMismatch = Try((response.json \ "CODE").as[String] == "CtReferenceMismatch").getOrElse(false)
      response.status match {
        case NO_CONTENT => Right(StoreCompanyNumberSuccess)
        case BAD_REQUEST if isCtMismatch => Left(CtReferenceMismatch)
        case status => Left(StoreCompanyNumberFailureResponse(status))
      }
    }
  }

  sealed trait StoreCompanyNumberFailure {
    def status: Int
  }

  case object StoreCompanyNumberSuccess

  case object CtReferenceMismatch extends StoreCompanyNumberFailure {
    override def status = BAD_REQUEST
  }

  case class StoreCompanyNumberFailureResponse(status: Int) extends StoreCompanyNumberFailure

}


