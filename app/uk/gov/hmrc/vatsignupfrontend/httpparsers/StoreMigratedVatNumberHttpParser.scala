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
import uk.gov.hmrc.http.{HttpReads, HttpResponse, InternalServerException}


object StoreMigratedVatNumberHttpParser {
  type StoreMigratedVatNumberResponse = Either[StoreMigratedVatNumberFailure, StoreMigratedVatNumberSuccess.type]

  val NoRelationshipCode = "RELATIONSHIP_NOT_FOUND"

  implicit object StoreMigratedVatNumberHttpReads extends HttpReads[StoreMigratedVatNumberResponse] {
    override def read(method: String, url: String, response: HttpResponse): StoreMigratedVatNumberResponse = {

      def responseCode: Option[String] = (response.json \ "CODE").asOpt[String]

      response.status match {
        case OK =>
          Right(StoreMigratedVatNumberSuccess)
        case FORBIDDEN if responseCode contains NoRelationshipCode =>
          Left(NoAgentClientRelationship)
        case FORBIDDEN =>
          Left(KnownFactsMismatch)
        case status =>
          throw new InternalServerException(s"[StoreMigratedVatNumber] connector returned an invalid response: $status ${response.body}")
      }
    }
  }

  case object StoreMigratedVatNumberSuccess

  sealed trait StoreMigratedVatNumberFailure

  case object KnownFactsMismatch extends StoreMigratedVatNumberFailure

  case object NoAgentClientRelationship extends StoreMigratedVatNumberFailure

}





