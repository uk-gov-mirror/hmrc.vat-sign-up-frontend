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
import uk.gov.hmrc.vatsubscriptionfrontend.Constants.skipIvJourneyValue
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys._
import uk.gov.hmrc.vatsubscriptionfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsubscriptionfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsubscriptionfrontend.httpparsers.StoreIdentityVerificationHttpParser.IdentityVerified
import uk.gov.hmrc.vatsubscriptionfrontend.models.{BusinessEntity, LimitedCompany, Other, SoleTrader}
import uk.gov.hmrc.vatsubscriptionfrontend.services.StoreIdentityVerificationService
import uk.gov.hmrc.vatsubscriptionfrontend.utils.SessionUtils._

import scala.concurrent.Future

@Singleton
class IdentityVerificationCallbackController @Inject()(val controllerComponents: ControllerComponents,
                                                       storeIdentityVerificationService: StoreIdentityVerificationService)
  extends AuthenticatedController(AdministratorRolePredicate) {

  val continue: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      (
        request.session.get(vatNumberKey),
        request.session.getModel[BusinessEntity](businessEntityKey),
        request.session.get(identityVerificationContinueUrlKey)
      ) match {
        case (Some(_), Some(Other), Some(_))  => Future.successful(Redirect(routes.CaptureBusinessEntityController.show()))
        case (Some(vatNumber), Some(businessEntity), Some(journeyLink)) =>

          storeIdentityVerificationService.storeIdentityVerification(vatNumber, journeyLink) map {
            case Right(IdentityVerified) =>
              if(journeyLink == skipIvJourneyValue){
                businessEntity match {
                  case LimitedCompany => Redirect(routes.CaptureCompanyNumberController.show())
                  case SoleTrader => Redirect(routes.AgreeCaptureEmailController.show())
                }
              }
              else Redirect(routes.IdentityVerificationSuccessController.show())
            case _ =>
              Redirect(routes.FailedIdentityVerificationController.show())
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
