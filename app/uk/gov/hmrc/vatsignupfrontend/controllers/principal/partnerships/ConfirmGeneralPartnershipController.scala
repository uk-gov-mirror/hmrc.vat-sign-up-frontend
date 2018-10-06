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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.GeneralPartnership
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.forms.ConfirmGeneralPartnershipForm._
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.confirm_general_partnership_sautr

import scala.concurrent.Future

@Singleton
class ConfirmGeneralPartnershipController @Inject()(val controllerComponents: ControllerComponents) // TODO add StorePartnershipSautrService
  extends AuthenticatedController(AdministratorRolePredicate, featureSwitches = Set(GeneralPartnership)) {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val optPartnershipSautr = request.session.get(SessionKeys.partnershipSautrKey).filter(_.nonEmpty)

      (optVatNumber, optPartnershipSautr) match {
        case (Some(_), Some(partnershipSautr)) =>
          Future.successful(
            Ok(confirm_general_partnership_sautr(partnershipSautr, confirmGeneralPartnershipForm, routes.ConfirmGeneralPartnershipController.submit()))
          )
        case (None, _) =>
          Future.successful(
            Redirect(principalRoutes.ResolveVatNumberController.resolve())
          )
        case _ =>
          Future.successful(
            throw new InternalServerException("Cannot capture user's UTR") // TODO Create Capture partnership SAUTR flow
          )
      }
    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
    val optPartnershipSautr = request.session.get(SessionKeys.partnershipSautrKey).filter(_.nonEmpty)

    authorised() {
      (optVatNumber, optPartnershipSautr) match {
        case (Some(vatNumber), Some(partnershipSautr)) => // TODO Create storePartnershipSautrService
          Future.successful(NotImplemented)
        case (None, _) =>
          Future.successful(
            Redirect(principalRoutes.ResolveVatNumberController.resolve())
          )
        case _ =>
          Future.failed(
            throw new InternalServerException("Cannot capture user's UTR") // TODO Create Capture partnership SAUTR flow
          )
      }
    }
  }
}
