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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.bta

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.VatControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.BTAClaimSubscription
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.forms.VatRegistrationDateForm._
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.vat_registration_date

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureBtaVatRegistrationDateController @Inject()(implicit ec: ExecutionContext,
                                                          vcc: VatControllerComponents)
  extends AuthenticatedController(AdministratorRolePredicate, featureSwitches = Set(BTAClaimSubscription)) {

  def show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(
        Ok(vat_registration_date(vatRegistrationDateForm, routes.CaptureBtaVatRegistrationDateController.submit()))
      )
    }
  }

  def submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      vatRegistrationDateForm.bindFromRequest.fold(
        formWithErrors =>
          Future.successful(
            BadRequest(vat_registration_date(formWithErrors, routes.CaptureBtaVatRegistrationDateController.submit()))
          ),
        vatRegistrationDate =>
          Future.successful(Redirect(routes.BtaBusinessPostCodeController.show().url)
            .addingToSession(SessionKeys.vatRegistrationDateKey, vatRegistrationDate))
      )
    }
  }
}
