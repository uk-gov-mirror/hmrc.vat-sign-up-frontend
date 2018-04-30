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
import uk.gov.hmrc.vatsubscriptionfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsubscriptionfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsubscriptionfrontend.forms.UserDetailsForm._
import uk.gov.hmrc.vatsubscriptionfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsubscriptionfrontend.views.html.principal.your_details

import scala.concurrent.Future

@Singleton
class CaptureYourDetailsController @Inject()(val controllerComponents: ControllerComponents)
  extends AuthenticatedController(AdministratorRolePredicate) {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(
        Ok(your_details(userDetailsForm.form, routes.CaptureYourDetailsController.submit()))
      )
    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      userDetailsForm.bindFromRequest.fold(
        formWithErrors =>
          Future.successful(
            BadRequest(your_details(formWithErrors, routes.CaptureYourDetailsController.submit()))
          ),
        userDetails =>
          Future.successful(
            Redirect(routes.ConfirmYourDetailsController.show()).addingToSession(SessionKeys.userDetailsKey, userDetails)
          )
      )
    }
  }

}
