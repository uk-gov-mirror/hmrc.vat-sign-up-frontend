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
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys._
import uk.gov.hmrc.vatsubscriptionfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsubscriptionfrontend.httpparsers.StoreIdentityVerificationHttpParser.IdentityVerified
import uk.gov.hmrc.vatsubscriptionfrontend.models.{BusinessEntity, LimitedCompany, SoleTrader}
import uk.gov.hmrc.vatsubscriptionfrontend.services.StoreIdentityVerificationService
import uk.gov.hmrc.vatsubscriptionfrontend.utils.SessionUtils._

import scala.concurrent.Future

@Singleton
class IdentityVerificationCallbackController @Inject()(val controllerComponents: ControllerComponents,
                                                       storeIdentityVerificationService: StoreIdentityVerificationService)
  extends AuthenticatedController() {

  val continue: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      (
        request.session.get(vatNumberKey),
        request.session.getModel[BusinessEntity](businessEntityKey),
        request.session.get(identityVerificationContinueUrlKey)
      ) match {
        case (Some(vatNumber), Some(businessEntity), Some(continueUrl)) =>
          storeIdentityVerificationService.storeIdentityVerification(vatNumber, continueUrl) map {
            case Right(IdentityVerified) =>
              businessEntity match {
                case SoleTrader =>
                  Redirect(routes.CaptureEmailController.show())
                case LimitedCompany =>
                  //TODO - implement capture company number page
                  NotImplemented
              }
            case _ =>
              //TODO - implement IV failed page
              NotImplemented
          }
        case (None, _, _) =>
          Future.successful(
            Redirect(routes.YourVatNumberController.show())
          )
        case (_, None, _) =>
          Future.successful(
            Redirect(routes.CaptureBusinessEntityController.show())
          )
        case (_, _, None) =>
          Future.successful(
            Redirect(routes.CaptureYourDetailsController.show())
          )
      }
    }
  }
}
