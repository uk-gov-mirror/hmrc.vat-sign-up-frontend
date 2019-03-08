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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.SessionKeys._
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreNinoHttpParser.{NoVATNumberFailure, StoreNinoFailureResponse}
import uk.gov.hmrc.vatsignupfrontend.models.{IRSA, NinoSource, UserDetailsModel}
import uk.gov.hmrc.vatsignupfrontend.services.StoreNinoService
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.confirm_your_user_details

import scala.concurrent.Future


@Singleton
class ConfirmYourRetrievedUserDetailsController @Inject()(val controllerComponents: ControllerComponents,
                                                          storeNinoService: StoreNinoService)
  extends AuthenticatedController(AdministratorRolePredicate) {

  def show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optUserDetails = request.session.getModel[UserDetailsModel](userDetailsKey)

      optUserDetails match {
        case Some(userDetails) => {
          Future.successful(
            Ok(confirm_your_user_details(userDetails, routes.ConfirmYourRetrievedUserDetailsController.submit()))
          )
        }
        case None => Future.successful(Redirect(routes.CaptureBusinessEntityController.show()))
      }
    }
  }

  def submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {

      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val optUserDetails = request.session.getModel[UserDetailsModel](userDetailsKey)
      val optNinoSource = request.session.getModel[NinoSource](ninoSourceKey)

      (optVatNumber, optUserDetails, optNinoSource) match {
        case (Some(vatNumber), Some(userDetails), Some(ninoSource)) =>
          storeNinoService.storeNino(vatNumber, userDetails, ninoSource) flatMap {
            case Right(_) => Future.successful(Redirect(routes.DirectDebitResolverController.show()))
            case Left(NoVATNumberFailure) =>
              Future.failed(new InternalServerException(s"Failure calling store nino: vat number is not found"))
            case Left(StoreNinoFailureResponse(status)) =>
              Future.failed(new InternalServerException(s"Failure calling store nino: status=$status"))
            case Left(_) =>
              Future.failed(new InternalServerException(s"Failure calling store nino: failed matching when no matching call required"))
          }
        case (None, _, _) =>
          Future.successful(Redirect(routes.ResolveVatNumberController.resolve()))
        case _ =>
          Future.successful(Redirect(routes.CaptureBusinessEntityController.show()))
      }
    }
  }

}
