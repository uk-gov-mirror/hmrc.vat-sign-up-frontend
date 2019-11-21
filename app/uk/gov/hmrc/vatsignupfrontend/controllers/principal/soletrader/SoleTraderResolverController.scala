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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.soletrader

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.auth.core.retrieve.{Retrievals, ~}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.services.StoreNinoService

import scala.concurrent.Future

@Singleton
class SoleTraderResolverController @Inject()(val controllerComponents: ControllerComponents,
                                             storeNinoService: StoreNinoService)
  extends AuthenticatedController(AdministratorRolePredicate) {

  val resolve: Action[AnyContent] = Action.async {
    implicit request =>
      authorised()(Retrievals.nino) {
        optNino =>
          val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)

          (optVatNumber, optNino) match {
            case (None, _) =>
              Future.successful(Redirect(principalRoutes.CaptureVatNumberController.show()))
            case (_, None) =>
              Future.successful(Redirect(routes.CaptureNinoController.show()))
            case (Some(vatNumber), Some(nino)) =>
              storeNinoService.storeNino(vatNumber, nino) map {
                case Right(_) =>
                  Redirect(principalRoutes.DirectDebitResolverController.show())
                case Left(reason) =>
                  throw new InternalServerException(s"Failed to store NINO with reason $reason")
              }
          }
      }
  }
}
