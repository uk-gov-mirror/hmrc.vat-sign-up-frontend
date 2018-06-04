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
import play.api.libs.json.{Json, OWrites, Reads}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object IdentityVerificationProxyHttpParser {

  type IdentityVerificationProxyResponse = Either[IdentityVerificationProxyFailureResponse, IdentityVerificationProxySuccessResponse]

  implicit object IdentityVerificationProxyHttpReads extends HttpReads[IdentityVerificationProxyResponse] {
    override def read(method: String, url: String, response: HttpResponse): IdentityVerificationProxyResponse =
      response.status match {
        case CREATED => Right(response.json.as[IdentityVerificationProxySuccessResponse](IdentityVerificationProxySuccessResponse.reader))
        case status => Left(IdentityVerificationProxyFailureResponse(status))
      }
  }

  case class IdentityVerificationProxySuccessResponse(link: String, journeyLink: String)

  object IdentityVerificationProxySuccessResponse {
    implicit val reader: Reads[IdentityVerificationProxySuccessResponse] = Json.reads[IdentityVerificationProxySuccessResponse]
    implicit val writer: OWrites[IdentityVerificationProxySuccessResponse] = Json.writes[IdentityVerificationProxySuccessResponse]
  }

  case class IdentityVerificationProxyFailureResponse(status: Int)

}
