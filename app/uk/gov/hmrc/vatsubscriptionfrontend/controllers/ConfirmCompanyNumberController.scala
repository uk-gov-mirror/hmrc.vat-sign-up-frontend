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

package uk.gov.hmrc.vatsubscriptionfrontend.controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys
import uk.gov.hmrc.vatsubscriptionfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.services.StoreCompanyNumberService

import scala.concurrent.Future

@Singleton
class ConfirmCompanyNumberController @Inject()(val controllerComponents: ControllerComponents,
                                               val storeCompanyNumberService: StoreCompanyNumberService)
  extends AuthenticatedController {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      request.session.get(SessionKeys.companyNumberKey) match {
        case Some(companyNumber) =>
          Future.successful(
            NotImplemented
          )
        case _ =>
          Future.successful(
            Redirect(routes.CaptureCompanyNumberController.show())
          )
      }
    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      request.session.get(SessionKeys.companyNumberKey) match {
        case Some(companyNumber) if companyNumber.nonEmpty =>
          storeCompanyNumberService.storeCompanyNumber(companyNumber) map {
            // TODO goto email
            case Right(_) =>
              NotImplemented
            case Left(errResponse) =>
              throw new InternalServerException("storeCompanyNumber failed: status=" + errResponse.status)
          }
        case _ =>
          Future.successful(
            Redirect(routes.CaptureCompanyNumberController.show())
          )
      }
    }
  }
}