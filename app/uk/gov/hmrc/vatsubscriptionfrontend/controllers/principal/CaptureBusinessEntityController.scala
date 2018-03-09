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
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys
import uk.gov.hmrc.vatsubscriptionfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsubscriptionfrontend.forms.BusinessEntityForm._
import uk.gov.hmrc.vatsubscriptionfrontend.httpparsers.IdentityVerificationProxyFailureResponse
import uk.gov.hmrc.vatsubscriptionfrontend.services.IdentityVerificationService
import uk.gov.hmrc.vatsubscriptionfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsubscriptionfrontend.views.html.principal.capture_business_entity

import scala.concurrent.Future

@Singleton
class CaptureBusinessEntityController @Inject()(val controllerComponents: ControllerComponents,
                                                identityVerificationService: IdentityVerificationService)
  extends AuthenticatedController() {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(
        Ok(capture_business_entity(businessEntityForm, routes.CaptureBusinessEntityController.submit()))
      )
    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      businessEntityForm.bindFromRequest.fold(
        formWithErrors =>
          Future.successful(
            BadRequest(capture_business_entity(formWithErrors, routes.CaptureBusinessEntityController.submit()))
          ),
        businessEntity =>
          identityVerificationService.start().map {
            case Right(response) =>
              val redirectionLocation = appConfig.identityVerificationFrontendRedirectionUrl(response.link)
              Redirect(redirectionLocation)
                .addingToSession(SessionKeys.businessEntityKey, businessEntity)
                .addingToSession(SessionKeys.identityVerificationJourneyKey, response)
            case Left(IdentityVerificationProxyFailureResponse(status)) =>
              throw new InternalServerException("CaptureBusinessEntityController.submit: identity verification returned failure " + status)
          }
      )
    }
  }
}