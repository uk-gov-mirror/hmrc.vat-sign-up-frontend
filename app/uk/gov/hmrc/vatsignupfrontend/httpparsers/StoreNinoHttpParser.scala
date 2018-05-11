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

package uk.gov.hmrc.vatsignupfrontend.httpparsers

import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object StoreNinoHttpParser {
  type StoreNinoResponse = Either[StoreNinoFailure, StoreNinoSuccess.type]

  implicit object StoreNinoHttpReads extends HttpReads[StoreNinoResponse] {
    override def read(method: String, url: String, response: HttpResponse): StoreNinoResponse =
      response.status match {
        case NO_CONTENT => Right(StoreNinoSuccess)
        case FORBIDDEN => Left(NoMatchFoundFailure)
        case NOT_FOUND => Left(NoVATNumberFailure)
        case status => Left(StoreNinoFailureResponse(status))
      }
  }

}


case object StoreNinoSuccess

sealed trait StoreNinoFailure

case object NoMatchFoundFailure extends StoreNinoFailure

case object NoVATNumberFailure extends StoreNinoFailure

case class StoreNinoFailureResponse(status: Int) extends StoreNinoFailure
