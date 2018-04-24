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

package uk.gov.hmrc.vatsubscriptionfrontend.controllers.principal

import javax.inject.{Inject, Singleton}

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys
import uk.gov.hmrc.vatsubscriptionfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.config.featureswitch.KnownFactsJourney
import uk.gov.hmrc.vatsubscriptionfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsubscriptionfrontend.models.{BusinessEntity, DateModel, LimitedCompany, SoleTrader}
import uk.gov.hmrc.vatsubscriptionfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsubscriptionfrontend.views.html.principal.check_your_answers

import scala.concurrent.Future

@Singleton
class CheckYourAnswersController @Inject()(val controllerComponents: ControllerComponents)
  extends AuthenticatedController(featureSwitches = Set(KnownFactsJourney)) {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val optVatRegistrationDate = request.session.getModel[DateModel](SessionKeys.vatRegistrationDateKey)
      val optBusinessPostCode = request.session.get(SessionKeys.businessPostCodeKey).filter(_.nonEmpty)
      val optBusinessEntity = request.session.getModel[BusinessEntity](SessionKeys.businessEntityKey)

      (optVatNumber, optVatRegistrationDate, optBusinessPostCode, optBusinessEntity) match {
        case (Some(vat_number), Some(vatRegistrationDate), Some(postCode), Some(entity@(SoleTrader | LimitedCompany))) =>
          Future.successful(
            Ok(check_your_answers(
              vat_number,
              vatRegistrationDate,
              postCode,
              entity,
              routes.CheckYourAnswersController.submit()))
          )
        case (None, _, _, _) =>
          Future.successful(
            Redirect(routes.CaptureVatNumberController.show())
          )
        case (_, None, _, _) =>
          Future.successful(
            Redirect(routes.CaptureVatRegistrationDateController.show())
          )
        case (_, _, None, _) =>
          Future.successful(
            Redirect(routes.BusinessPostCodeController.show())
          )
        case _ =>
          Future.successful(
            Redirect(routes.CaptureBusinessEntityController.show())
          )
      }
    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val optVatRegistrationDate = request.session.getModel[DateModel](SessionKeys.vatRegistrationDateKey)
      val optBusinessPostCode = request.session.get(SessionKeys.businessPostCodeKey).filter(_.nonEmpty)
      val optBusinessEntity = request.session.getModel[BusinessEntity](SessionKeys.businessEntityKey)

      (optVatNumber, optVatRegistrationDate, optBusinessPostCode, optBusinessEntity) match {
        case (Some(vat_number), Some(vatRegistrationDate), Some(postCode), Some(entity@(SoleTrader | LimitedCompany))) =>
          // TODO call check known facts instead when it's ready
          Future.successful(
            Redirect(routes.CaptureYourDetailsController.show())
          )
        case (None, _, _, _) =>
          Future.successful(
            Redirect(routes.CaptureVatNumberController.show())
          )
        case (_, None, _, _) =>
          Future.successful(
            Redirect(routes.CaptureVatRegistrationDateController.show())
          )
        case (_, _, None, _) =>
          Future.successful(
            Redirect(routes.BusinessPostCodeController.show())
          )
        case _ =>
          Future.successful(
            Redirect(routes.CaptureBusinessEntityController.show())
          )
      }
    }
  }
}
