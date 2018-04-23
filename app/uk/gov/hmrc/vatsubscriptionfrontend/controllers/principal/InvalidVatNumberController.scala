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
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.vatsubscriptionfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.config.featureswitch._
import uk.gov.hmrc.vatsubscriptionfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsubscriptionfrontend.views.html.principal.could_not_confirm_vat_number

import scala.concurrent.Future

@Singleton
class InvalidVatNumberController @Inject()(val controllerComponents: ControllerComponents)
  extends AuthenticatedController(featureSwitches = Set(KnownFactsJourney)) {

  def show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(
        Ok(could_not_confirm_vat_number(routes.InvalidVatNumberController.submit()))
      )
    }
  }

  def submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(
        //todo Redirect to capture vat number
        NotImplemented
      )
    }
  }
}
