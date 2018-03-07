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

package uk.gov.hmrc.vatsubscriptionfrontend.controllers.individual

import javax.inject.{Inject, Singleton}

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.hmrc.vatsubscriptionfrontend.config.{AppConfig, ControllerComponents}
import uk.gov.hmrc.vatsubscriptionfrontend.views.html.individual.your_vat_number

@Singleton
class YourVatNumberController @Inject()(val controllerComponents: ControllerComponents) extends Controller with I18nSupport{

  override val messagesApi: MessagesApi = controllerComponents.messagesApi
  implicit val appConfig: AppConfig = controllerComponents.appConfig

  val show: Action[AnyContent] = Action { implicit request =>
    Ok(your_vat_number("32423423234", routes.YourVatNumberController.submit()))
  }

  val submit: Action[AnyContent] = Action { implicit request =>
   NotImplemented
  }

}