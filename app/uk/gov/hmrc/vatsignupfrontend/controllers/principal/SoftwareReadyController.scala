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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.vatsignupfrontend.config.{AppConfig, ControllerComponents}
import uk.gov.hmrc.vatsignupfrontend.forms.SoftwareReadyForm._
import uk.gov.hmrc.vatsignupfrontend.models.{No, Yes}
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.software_ready

import scala.concurrent.Future

@Singleton
class SoftwareReadyController @Inject()(val controllerComponents: ControllerComponents) extends FrontendController with I18nSupport {

  override val messagesApi: MessagesApi = controllerComponents.messagesApi

  implicit val appConfig: AppConfig = controllerComponents.appConfig

  val show: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(
      Ok(software_ready(softwareReadyForm, routes.SoftwareReadyController.submit()))
    )
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    softwareReadyForm.bindFromRequest.fold(
      formWithErrors =>
        Future.successful(
          BadRequest(software_ready(formWithErrors, routes.SoftwareReadyController.submit()))
        ), {
        case Yes =>
          Future.successful(Redirect(routes.ResolveVatNumberController.resolve()))
        case No =>
          Future.successful(Redirect(routes.VerifySoftwareErrorController.show()))
      }
    )
  }
}
