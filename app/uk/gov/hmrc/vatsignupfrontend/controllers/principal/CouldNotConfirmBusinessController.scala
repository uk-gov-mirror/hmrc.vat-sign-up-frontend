/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.vatsignupfrontend.SessionKeys._
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.error.{routes => errorRoutes}
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.could_not_confirm_business

import scala.concurrent.Future

@Singleton
class CouldNotConfirmBusinessController @Inject()(val controllerComponents: ControllerComponents)
  extends AuthenticatedController(AdministratorRolePredicate) {

  def show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(
        Ok(could_not_confirm_business(errorRoutes.CouldNotConfirmBusinessController.submit())))
    }
  }

  def submit: Action[AnyContent] = Action.async { implicit request =>
    val optBusinessEntity = request.session.getModel[BusinessEntity](businessEntityKey)

    authorised() {
      Future.successful(
        optBusinessEntity match {
          case Some(_: BusinessEntity) => Redirect(routes.CaptureBusinessEntityController.show().url).removingFromSession(
            businessEntityKey,
            companyNumberKey,
            companyUtrKey,
            ninoKey,
            partnershipSautrKey
          )
          case _ => Redirect(routes.CaptureVatNumberController.show().url)
        }
      )
    }
  }

}
