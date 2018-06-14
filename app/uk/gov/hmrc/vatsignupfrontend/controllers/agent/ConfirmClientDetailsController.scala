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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import javax.inject.{Inject, Singleton}

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.SessionKeys.userDetailsKey
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AgentEnrolmentPredicate
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreNinoHttpParser.{NoMatchFoundFailure, NoVATNumberFailure, StoreNinoFailureResponse}
import uk.gov.hmrc.vatsignupfrontend.models.{UserDetailsModel, UserEntered}
import uk.gov.hmrc.vatsignupfrontend.services.StoreNinoService
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsignupfrontend.views.html.agent.check_your_client_details

import scala.concurrent.Future

@Singleton
class ConfirmClientDetailsController @Inject()(val controllerComponents: ControllerComponents,
                                               val storeNinoService: StoreNinoService)
  extends AuthenticatedController(AgentEnrolmentPredicate) {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val optUserDetails = request.session.getModel[UserDetailsModel](userDetailsKey)

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
      val optUserDetails = request.session.getModel[UserDetailsModel](userDetailsKey)

      (optVatNumber, optUserDetails) match {
        case (None, _) => Future.successful(Redirect(routes.CaptureVatNumberController.show()))
        case (_, None) => Future.successful(Redirect(routes.CaptureClientDetailsController.show()))
        case (Some(vatNumber), Some(userDetails)) => {
          storeNinoService.storeNino(vatNumber, userDetails, Some(UserEntered)) map {
            case Right(_) => Redirect(routes.EmailRoutingController.route())
            case Left(NoMatchFoundFailure) => Redirect(routes.FailedClientMatchingController.show())
            case Left(NoVATNumberFailure) => throw new InternalServerException(s"Failure calling store nino: vat number is not found")
            case Left(StoreNinoFailureResponse(status)) => throw new InternalServerException(s"Failure calling store nino: status=$status")
          }
        }.map(_.removingFromSession(userDetailsKey))
      }
    }
  }

}
