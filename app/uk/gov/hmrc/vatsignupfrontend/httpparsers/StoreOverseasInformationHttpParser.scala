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

import play.api.http.Status.NO_CONTENT
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object StoreOverseasInformationHttpParser {
  type StoreOverseasInformationResponse = Either[StoreOverseasInformationFailure, StoreOverseasInformationSuccess.type]

  implicit object StoreOverseasInformationHttpReads extends HttpReads[StoreOverseasInformationResponse] {
    override def read(method: String, url: String, response: HttpResponse): StoreOverseasInformationResponse =
      response.status match {
        case NO_CONTENT => Right(StoreOverseasInformationSuccess)
        case status => Left(StoreOverseasInformationFailureResponse(status))
      }
  }

  case object StoreOverseasInformationSuccess

  sealed trait StoreOverseasInformationFailure

  case class StoreOverseasInformationFailureResponse(status: Int) extends StoreOverseasInformationFailure

}
