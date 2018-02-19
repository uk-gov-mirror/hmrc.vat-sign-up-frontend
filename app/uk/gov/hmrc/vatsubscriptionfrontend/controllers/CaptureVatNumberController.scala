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

package uk.gov.hmrc.vatsubscriptionfrontend.controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.vatsubscriptionfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.forms.VatNumberForm._
import uk.gov.hmrc.vatsubscriptionfrontend.services.StoreSubscriptionDetailsService
import uk.gov.hmrc.vatsubscriptionfrontend.views.html.capture_vat_number

import scala.concurrent.Future

@Singleton
class CaptureVatNumberController @Inject()(val controllerComponents: ControllerComponents,
                                           val storeSubscriptionDetailsService: StoreSubscriptionDetailsService)
  extends AuthenticatedController {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(
        Ok(capture_vat_number(vatNumberForm, routes.CaptureVatNumberController.submit()))
      )
    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      vatNumberForm.bindFromRequest.fold(
        formWithErrors =>
          Future.successful(
            BadRequest(capture_vat_number(formWithErrors, routes.CaptureVatNumberController.submit()))
          ),
        vatNumber => //TODO store VAT Number
          storeSubscriptionDetailsService.storeVatNumber(vatNumber) map {
            case Right(_) => Ok("Success")
            case Left(_) => NotImplemented("Failure")
          }
      )
    }
  }
}