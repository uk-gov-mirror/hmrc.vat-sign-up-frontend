/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent.soletrader

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.vatsignupfrontend.SessionKeys.ninoKey
import uk.gov.hmrc.vatsignupfrontend.config.VatControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AgentEnrolmentPredicate
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.forms.NinoForm._
import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.PrevalidationAPI
import uk.gov.hmrc.vatsignupfrontend.views.html.agent.soletrader.agent_capture_nino

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class CaptureNinoController @Inject()(implicit ec: ExecutionContext,
                                        vcc: VatControllerComponents)
  extends AuthenticatedController(AgentEnrolmentPredicate) {

  val validateNinoForm: PrevalidationAPI[String] = ninoForm(isAgent = true)

  def show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(
        Ok(agent_capture_nino(validateNinoForm.form, routes.CaptureNinoController.submit()))
      )
    }
  }

  def submit: Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        validateNinoForm.bindFromRequest.fold(
          formWithErrors => {
            Future.successful(
              BadRequest(agent_capture_nino(formWithErrors, routes.CaptureNinoController.submit()))
            )
          },
          nino =>
            Future.successful(
              Redirect(routes.ConfirmNinoController.show()).addingToSession(ninoKey -> nino)
            )
        )
      }
  }
}
