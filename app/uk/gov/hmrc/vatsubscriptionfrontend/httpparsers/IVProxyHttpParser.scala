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

package uk.gov.hmrc.vatsubscriptionfrontend.httpparsers

import play.api.http.Status._
import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object IVProxyHttpParser {
  type IVProxyResponse = Either[IVFailureResponse, IVSuccessResponse]


  implicit object IVProxyHttpReads extends HttpReads[IVProxyResponse] {
    override def read(method: String, url: String, response: HttpResponse): IVProxyResponse =
      response.status match {
        case CREATED => Right(response.json.as[IVSuccessResponse](IVSuccessResponse.reader))
        case status => Left(IVFailureResponse(status))
      }
  }

}

case class IVSuccessResponse(link: String, journeyLink: String)

object IVSuccessResponse {
  val reader: Reads[IVSuccessResponse] = Json.reads[IVSuccessResponse]
}

case class IVFailureResponse(status: Int)
