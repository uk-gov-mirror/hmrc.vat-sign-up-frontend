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

package uk.gov.hmrc.vatsubscriptionfrontend.controllers.agent

import javax.inject.{Inject, Singleton}

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys
import uk.gov.hmrc.vatsubscriptionfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.config.auth.AgentEnrolmentPredicate
import uk.gov.hmrc.vatsubscriptionfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsubscriptionfrontend.httpparsers.{NoMatchFoundFailure, NoVATNumberFailure, StoreNinoFailureResponse}
import uk.gov.hmrc.vatsubscriptionfrontend.models.UserDetailsModel
import uk.gov.hmrc.vatsubscriptionfrontend.services.StoreNinoService
import uk.gov.hmrc.vatsubscriptionfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsubscriptionfrontend.views.html.agent.check_your_client_details

import scala.concurrent.Future

@Singleton
class ConfirmClientDetailsController @Inject()(val controllerComponents: ControllerComponents,
                                               val storeNinoService: StoreNinoService)
  extends AuthenticatedController(AgentEnrolmentPredicate) {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val optUserDetails = request.session.getJson[UserDetailsModel](SessionKeys.userDetailsKey)

      (optVatNumber, optUserDetails) match {
        case (None, _) => Future.successful(Redirect(routes.CaptureVatNumberController.show()))
        case (_, None) => Future.successful(Redirect(routes.CaptureClientDetailsController.show()))
        case (Some(vatNumber), Some(userDetails)) =>
          Future.successful(Ok(check_your_client_details(userDetails, routes.ConfirmClientDetailsController.submit())))
      }

    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val optUserDetails = request.session.getJson[UserDetailsModel](SessionKeys.userDetailsKey)

      (optVatNumber, optUserDetails) match {
        case (None, _) => Future.successful(Redirect(routes.CaptureVatNumberController.show()))
        case (_, None) => Future.successful(Redirect(routes.CaptureClientDetailsController.show()))
        case (Some(vatNumber), Some(userDetails)) =>
          storeNinoService.storeNino(vatNumber, userDetails) map {
            case Right(_) => Redirect(routes.AgreeCaptureEmailController.show())
            case Left(NoMatchFoundFailure) => throw new InternalServerException(s"Failure calling store nino: no match found")
            case Left(NoVATNumberFailure) => throw new InternalServerException(s"Failure calling store nino: vat number is not found")
            case Left(StoreNinoFailureResponse(status)) => throw new InternalServerException(s"Failure calling store nino: status=$status")
          }
      }
    }
  }

}
