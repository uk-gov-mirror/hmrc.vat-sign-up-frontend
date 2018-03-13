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
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.http.{BadGatewayException, InternalServerException}
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys.{identityVerificationContinueUrlKey, userDetailsKey}
import uk.gov.hmrc.vatsubscriptionfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsubscriptionfrontend.httpparsers.{NoMatchFoundFailure, NoVATNumberFailure, StoreNinoFailureResponse}
import uk.gov.hmrc.vatsubscriptionfrontend.models.UserDetailsModel
import uk.gov.hmrc.vatsubscriptionfrontend.services.{IdentityVerificationService, StoreNinoService}
import uk.gov.hmrc.vatsubscriptionfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsubscriptionfrontend.views.html.principal.check_your_details

import scala.concurrent.Future

@Singleton
class ConfirmYourDetailsController @Inject()(val controllerComponents: ControllerComponents,
                                             storeNinoService: StoreNinoService,
                                             identityVerificationService: IdentityVerificationService)
  extends AuthenticatedController() {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optUserDetails = request.session.getModel[UserDetailsModel](userDetailsKey)

      optUserDetails match {
        case Some(userDetails) =>
          Future.successful(Ok(check_your_details(userDetails, routes.ConfirmYourDetailsController.submit())))
        case None => Future.successful(Redirect(routes.CaptureYourDetailsController.show()))
      }

    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised()(Retrievals.confidenceLevel) {
      confidenceLevel =>
        val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
        val optUserDetails = request.session.getModel[UserDetailsModel](userDetailsKey)

        (optVatNumber, optUserDetails) match {
          case (Some(vatNumber), Some(userDetails)) =>
            storeNinoService.storeNino(vatNumber, userDetails) flatMap {
              case Right(_) =>
                if (confidenceLevel.compare(ConfidenceLevel.L200) < 0) {
                  identityVerificationService.start() map {
                    case Right(response) =>
                      val redirectUrl = appConfig.identityVerificationFrontendRedirectionUrl(response.link)

                      Redirect(redirectUrl)
                        .addingToSession(identityVerificationContinueUrlKey -> response.journeyLink)
                    case Left(error) =>
                      throw new BadGatewayException(s"Failure calling identity verification: status=${error.status}")
                  }
                } else Future.successful(Redirect(routes.CaptureEmailController.show()))
              case Left(NoMatchFoundFailure) =>
                Future.failed(new InternalServerException(s"Failure calling store nino: no match found"))
              case Left(NoVATNumberFailure) =>
                Future.failed(new InternalServerException(s"Failure calling store nino: vat number is not found"))
              case Left(StoreNinoFailureResponse(status)) =>
                Future.failed(new InternalServerException(s"Failure calling store nino: status=$status"))
            }
          case (None, _) =>
            Future.successful(Redirect(routes.YourVatNumberController.show()))
          case (_, None) =>
            Future.successful(Redirect(routes.CaptureYourDetailsController.show()))
        }
    } map (_ removingFromSession userDetailsKey)
  }

}
