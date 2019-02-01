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

package uk.gov.hmrc.vatsignupfrontend.httpparsers

import play.api.http.Status.NO_CONTENT
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object StoreGovOrgInformationHttpParser {
  type StoreGovOrgInformationResponse = Either[StoreGovOrgInformationFailure, StoreGovOrgInformationSuccess.type]

  implicit object StoreGovOrgInformationHttpReads extends HttpReads[StoreGovOrgInformationResponse] {
    override def read(method: String, url: String, response: HttpResponse): StoreGovOrgInformationResponse =
      response.status match {
        case NO_CONTENT => Right(StoreGovOrgInformationSuccess)
        case status => Left(StoreGovOrgInformationFailureResponse(status))
      }
  }

  case object StoreGovOrgInformationSuccess

  sealed trait StoreGovOrgInformationFailure

  case class StoreGovOrgInformationFailureResponse(status: Int) extends StoreGovOrgInformationFailure

}
