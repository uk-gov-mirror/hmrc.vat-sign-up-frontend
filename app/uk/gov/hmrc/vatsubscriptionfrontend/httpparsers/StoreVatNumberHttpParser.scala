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
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.vatsubscriptionfrontend.Constants.{StoreVatNumberNoRelationshipCodeKey, StoreVatNumberNoRelationshipCodeValue}
import uk.gov.hmrc.vatsubscriptionfrontend.models._

object StoreVatNumberHttpParser {
  type StoreVatNumberResponse = Either[StoreVatNumberFailure, StoreVatNumberSuccess.type]

  implicit object StoreVatNumberHttpReads extends HttpReads[StoreVatNumberResponse] {
    override def read(method: String, url: String, response: HttpResponse): StoreVatNumberResponse = {

      def parseBody: Option[String] = (response.json \ StoreVatNumberNoRelationshipCodeKey).asOpt[String]

      response.status match {
        case CREATED => Right(StoreVatNumberSuccess)
        case FORBIDDEN => parseBody match {
          case Some(code) if code.matches(StoreVatNumberNoRelationshipCodeValue) => Left(StoreVatNumberNoRelationship)
          case _ => Left(StoreVatNumberFailureResponse(FORBIDDEN))
        }
        case status => Left(StoreVatNumberFailureResponse(status))
      }
    }
  }
}


