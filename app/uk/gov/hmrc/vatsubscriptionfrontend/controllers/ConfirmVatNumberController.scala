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
import uk.gov.hmrc.vatsubscriptionfrontend.models.{StoreVatNumberNoRelationship, StoreVatNumberSuccess}
import uk.gov.hmrc.vatsubscriptionfrontend.services.StoreVatNumberService
import uk.gov.hmrc.vatsubscriptionfrontend.views.html.confirm_vat_number

import scala.concurrent.Future

@Singleton
class ConfirmVatNumberController @Inject()(val controllerComponents: ControllerComponents,
                                           val storeVatNumberService: StoreVatNumberService)
  extends AuthenticatedController(AgentEnrolmentPredicate) {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      request.session.get(SessionKeys.vatNumberKey) match {
        case Some(vatNumber) if vatNumber.nonEmpty =>
          Future.successful(
            Ok(confirm_vat_number(vatNumber, routes.ConfirmVatNumberController.submit()))
          )
        case _ =>
          Future.successful(
            Redirect(routes.CaptureVatNumberController.show())
          )
      }
    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      request.session.get(SessionKeys.vatNumberKey) match {
        case Some(vatNumber) if vatNumber.nonEmpty =>
          storeVatNumberService.storeVatNumber(vatNumber) map {
            case Right(StoreVatNumberSuccess) =>
              Redirect(routes.CaptureBusinessEntityController.show())
            case Right(StoreVatNumberNoRelationship) =>
              Redirect(routes.NoAgentClientRelationshipController.show())
            case Left(errResponse) =>
              throw new InternalServerException("storeVatNumber failed: status=" + errResponse.status)
          }
        case _ =>
          Future.successful(
            Redirect(routes.CaptureVatNumberController.show())
          )
      }
    }
  }

}