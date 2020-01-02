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
import uk.gov.hmrc.play.bootstrap.controller.{BaseController, FrontendController}
import uk.gov.hmrc.vatsignupfrontend.config.{AppConfig, ControllerComponents}
import uk.gov.hmrc.vatsignupfrontend.forms.HaveSoftwareForm._
import uk.gov.hmrc.vatsignupfrontend.models.{No, Yes}
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.have_software

import scala.concurrent.Future

@Singleton
class HaveSoftwareController @Inject()(val controllerComponents: ControllerComponents) extends FrontendController with I18nSupport {


  override val messagesApi: MessagesApi = controllerComponents.messagesApi

  implicit val appConfig: AppConfig = controllerComponents.appConfig

  val show: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(have_software(haveSoftwareForm, routes.HaveSoftwareController.submit())))
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    haveSoftwareForm.bindFromRequest.fold(
      formWithErrors =>
        Future.successful(
          BadRequest(have_software(formWithErrors, routes.HaveSoftwareController.submit()))
        ), {
        case Yes =>
          Future.successful(Redirect(routes.SoftwareReadyController.show()))
        case No =>
          Future.successful(Redirect(routes.ChooseSoftwareErrorController.show()))
      }
    )
  }

}
