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
import uk.gov.hmrc.vatsubscriptionfrontend.config.auth.AgentEnrolmentPredicate
import uk.gov.hmrc.vatsubscriptionfrontend.services.StoreEmailAddressService
import uk.gov.hmrc.vatsubscriptionfrontend.views.html.confirm_email

import scala.concurrent.Future

@Singleton
class ConfirmEmailController @Inject()(val controllerComponents: ControllerComponents,
                                       val storeEmailAddressService: StoreEmailAddressService)
  extends AuthenticatedController {
class ConfirmEmailController @Inject()(val controllerComponents: ControllerComponents)
  extends AuthenticatedController(AgentEnrolmentPredicate) {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val optEmail = request.session.get(SessionKeys.emailKey).filter(_.nonEmpty)

      (optVatNumber, optEmail) match {
        case (Some(_), Some(email)) =>
          Future.successful(
            Ok(confirm_email(email, routes.ConfirmEmailController.submit()))
          )
        case (None, _) =>
          Future.successful(
            Redirect(routes.CaptureVatNumberController.show())
          )
        case _ =>
          Future.successful(
            Redirect(routes.CaptureEmailController.show())
          )
      }
    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val optEmail = request.session.get(SessionKeys.emailKey).filter(_.nonEmpty)

      (optVatNumber, optEmail) match {
        case (Some(vatNumber), Some(email)) =>
          storeEmailAddressService.storeEmailAddress(vatNumber, email) map {
            case Right(_) =>
              Redirect(routes.VerifyEmailController.show().url)
            case Left(errResponse) =>
              throw new InternalServerException("storeEmailAddress failed: status=" + errResponse.status)
          }
        case (None, _) =>
          Future.successful(
            Redirect(routes.CaptureVatNumberController.show())
          )
        case _ =>
          Future.successful(
            Redirect(routes.CaptureEmailController.show())
          )
      }
    }
  }

}
