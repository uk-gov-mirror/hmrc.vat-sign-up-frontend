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

package uk.gov.hmrc.vatsignupfrontend.controllers

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Result
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.authorise.{EmptyPredicate, Predicate}
import uk.gov.hmrc.auth.core.retrieve.{EmptyRetrieval, Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{FeatureSwitch, FeatureSwitchedController}
import uk.gov.hmrc.vatsignupfrontend.config.{AppConfig, ControllerComponents}

import scala.concurrent.Future

abstract class AuthenticatedController[A](retrievalPredicate: RetrievalPredicate[A] = EmptyRetrievalPredicate,
                                          override val featureSwitches: Set[FeatureSwitch] = Set.empty)
  extends FeatureSwitchedController with FrontendController with I18nSupport {

  def authorised(predicate: Predicate = EmptyPredicate): AuthorisedFunction =
    featureEnabled[AuthorisedFunction](new AuthorisedFunction(predicate))

  class AuthorisedFunction(predicate: Predicate, filter: => Boolean = true) {
    def apply(block: => Future[Result])(implicit hc: HeaderCarrier): Future[Result] =
      authConnector.authorise(predicate, retrievalPredicate.retrieval) flatMap {
        retrieval =>
          retrievalPredicate.function(block)(retrieval)
      }

    def apply[B](retrieval: Retrieval[B])(block: B => Future[Result])(implicit hc: HeaderCarrier): Future[Result] =
      authConnector.authorise(predicate, retrievalPredicate.retrieval and retrieval) flatMap {
        case retrievalA ~ retrievalB =>
          retrievalPredicate.function(block(retrievalB))(retrievalA)
      }
  }

  def controllerComponents: ControllerComponents

  override val messagesApi: MessagesApi = controllerComponents.messagesApi

  val authConnector: AuthConnector = controllerComponents.authConnector

  implicit val appConfig: AppConfig = controllerComponents.appConfig
}

trait RetrievalPredicate[A] {
  original =>
  def retrieval: Retrieval[A]

  def function(block: => Future[Result]): A => Future[Result]

  def and[B](newRetrievalPredicate: RetrievalPredicate[B]): RetrievalPredicate[A ~ B] = {
    new RetrievalPredicate[~[A, B]] {
      override def retrieval: Retrieval[A ~ B] =
        original.retrieval and newRetrievalPredicate.retrieval

      override def function(block: => Future[Result]): ~[A, B] => Future[Result] = {
        case retrievalA ~ retrievalB =>
          val originalResult = original.function(block)(retrievalA)
          val newResult = newRetrievalPredicate.function(originalResult)(retrievalB)

          newResult
      }
    }
  }
}

object EmptyRetrievalPredicate extends RetrievalPredicate[Unit] {
  override def retrieval: Retrieval[Unit] = EmptyRetrieval

  override def function(block: => Future[Result]): Unit => Future[Result] = _ => block
}
