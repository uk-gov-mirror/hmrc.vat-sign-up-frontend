/*
 * Copyright 2019 HM Revenue & Customs
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
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AgentEnrolmentPredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.SkipCidCheck
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.{routes => agentRoutes}
import uk.gov.hmrc.vatsignupfrontend.forms.NinoForm._
import uk.gov.hmrc.vatsignupfrontend.views.html.agent.soletrader.agent_capture_nino

import scala.concurrent.Future


@Singleton
class CaptureNinoController @Inject()(val controllerComponents: ControllerComponents)
  extends AuthenticatedController(AgentEnrolmentPredicate, featureSwitches = Set(SkipCidCheck)) {

  def show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(
        Ok(agent_capture_nino(ninoForm.form, routes.CaptureNinoController.submit()))
      )
    }
  }

  def submit: Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        ninoForm.bindFromRequest.fold (
          formWithErrors => {
            Future.successful(
              BadRequest(agent_capture_nino(formWithErrors, routes.CaptureNinoController.submit()))
            )
          },
          // TODO to implement the Redirect page
          nino => {
            Future.successful(NotImplemented)
          }
        )
      }
  }
}
