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
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys.businessEntityKey
import uk.gov.hmrc.vatsubscriptionfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsubscriptionfrontend.models.{BusinessEntity, LimitedCompany, SoleTrader}
import uk.gov.hmrc.vatsubscriptionfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsubscriptionfrontend.views.html.principal.identity_verification_success

import scala.concurrent.Future

@Singleton
class IdentityVerificationSuccessController @Inject()(val controllerComponents: ControllerComponents)
  extends AuthenticatedController() {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(
        Ok(identity_verification_success(routes.IdentityVerificationSuccessController.submit()))
      )
    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val businessEntity = request.session.getModel[BusinessEntity](businessEntityKey)
      Future.successful(
        businessEntity match {
          case Some(SoleTrader) =>
            Redirect(routes.AgreeCaptureEmailController.show())
          case Some(LimitedCompany) =>
            Redirect(routes.CaptureCompanyNumberController.show())
          case _ =>
            Redirect(routes.CaptureBusinessEntityController.show())
        }
      )
    }
  }

}
