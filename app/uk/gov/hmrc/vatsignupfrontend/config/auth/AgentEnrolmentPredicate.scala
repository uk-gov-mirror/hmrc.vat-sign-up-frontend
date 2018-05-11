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

package uk.gov.hmrc.vatsignupfrontend.config.auth

import play.api.mvc.Result
import play.api.mvc.Results._
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, Retrievals}
import uk.gov.hmrc.vatsignupfrontend.controllers.RetrievalPredicate
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.routes
import uk.gov.hmrc.vatsignupfrontend.Constants.Enrolments.agentEnrolmentKey

import scala.concurrent.Future

object AgentEnrolmentPredicate extends RetrievalPredicate[Enrolments] {

  override def retrieval: Retrieval[Enrolments] = Retrievals.allEnrolments

  override def function(block: => Future[Result]): Enrolments => Future[Result] =
    enrolments =>
      if (enrolments.getEnrolment(agentEnrolmentKey).isDefined) block
      else Future.successful(
        Redirect(routes.NotEnrolledToAgentServicesController.show())
      )
}
