/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.actions

import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import models.Regime
import models.requests.AuthorisedRequest
import play.api.Logging
import play.api.mvc.*
import play.api.mvc.Results.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[DefaultAuthorisedAction])
trait AuthorisedAction extends ActionBuilder[AuthorisedRequest, AnyContent] with ActionFunction[Request, AuthorisedRequest]

@Singleton
class DefaultAuthorisedAction @Inject() (
  override val authConnector: AuthConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends AuthorisedAction
    with AuthorisedFunctions
    with Logging {

  override def invokeBlock[A](request: Request[A], block: AuthorisedRequest[A] => Future[Result]): Future[Result] = {

    given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised()
      .retrieve(Retrievals.affinityGroup.and(Retrievals.allEnrolments)) {
        case Some(affinityGroup @ AffinityGroup.Agent) ~ AuthorisedAction.HasActiveAgentEnrolment(
              regNum,
              regime
            ) =>
          block(AuthorisedRequest(request, affinityGroup, regNum, regime))
        case Some(AffinityGroup.Agent) ~ _ =>
          logger.warn(s"Agent auth failed: enrolment missing or not activated for ${request.path}")
          Future.failed(InsufficientEnrolments("Agent enrolment missing or not activated"))

        case Some(affinityGroup @ AffinityGroup.Organisation) ~ AuthorisedAction.HasActiveOrganisationEnrolment(
              regNum,
              regime
            ) =>
          if (ValidationAction.validateRegimeRegNo(regime, regNum)) {
            block(AuthorisedRequest(request, affinityGroup, regNum, regime))
          } else {
            Future.successful(Redirect(controllers.routes.AccessDeniedController.onPageLoad()))
          }
        case Some(AffinityGroup.Organisation) ~ _ =>
          logger.warn(s"Organisation auth failed: enrolment missing or not activated for ${request.path}")
          Future.failed(InsufficientEnrolments("Organisation enrolment missing or not activated"))

        case _ =>
          logger.warn(s"Auth failed: no affinity group found for ${request.path}")
          Future.failed(UnsupportedAffinityGroup("No affinity group found"))

      }
      .recover {
        case _: InsufficientEnrolments | _: UnsupportedAffinityGroup | _: InsufficientConfidenceLevel | _: UnsupportedCredentialRole |
            _: UnsupportedAuthProvider =>
          Redirect(controllers.routes.AccessDeniedController.onPageLoad())
        case ex: AuthorisationException =>
          logger.info(s"Unauthenticated access to ${request.path}: ${ex.getMessage}")
          Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
      }
  }
}

object AuthorisedAction {

  private val organisationEnrolments: Seq[(String, String, Regime)] = Seq(
    ("HMRC-MGD-ORG", "HMRCMGDRN", Regime.MGD),
    ("HMRC-GTS-GBD", "HMRCGTSGBRN", Regime.GBD),
    ("HMRC-GTS-PBD", "HMRCGTSGBRN", Regime.PBD),
    ("HMRC-GTS-RGD", "HMRCGTSGBRN", Regime.RGD)
  )

  private val agentEnrolments: Seq[(String, String, Regime)] = Seq(
    ("HMRC-MGD-AGNT", "HMRCMGDAGENTREF", Regime.MGD),
    ("HMRC-GTS-AGNT", "HMRCGTSAGENTREF", Regime.GBD)
  )

  private def getEnrolmentIdentifier(
    enrolments: Enrolments,
    enrolmentKey: String,
    identifierName: String
  ): Option[String] =
    enrolments.getEnrolment(enrolmentKey).flatMap { enrolment =>
      Option
        .when(enrolment.isActivated)(
          enrolment.getIdentifier(identifierName).map(_.value)
        )
        .flatten
        .filter(_.nonEmpty)
    }

  private def findActiveEnrolment(enrolments: Enrolments, candidates: Seq[(String, String, Regime)]): Option[(String, Regime)] =
    candidates.view.flatMap { case (key, identifier, regime) =>
      getEnrolmentIdentifier(enrolments, key, identifier).map(_ -> regime)
    }.headOption

  object HasActiveAgentEnrolment {
    def unapply(enrolments: Enrolments): Option[(String, Regime)] =
      findActiveEnrolment(enrolments, agentEnrolments)
  }

  object HasActiveOrganisationEnrolment {
    def unapply(enrolments: Enrolments): Option[(String, Regime)] =
      findActiveEnrolment(enrolments, organisationEnrolments)
  }

}

object ValidationAction extends Logging {
  private val REF_NO_LENGTH = 7
  private val regEx = "X[A-Z]{1}[A-Z]{1}[0-9]{11}"

  private val WEIGHT_9 = 9
  private val WEIGHT_10 = 10
  private val WEIGHT_11 = 11
  private val WEIGHT_12 = 12
  private val WEIGHT_13 = 13
  private val WEIGHT_8 = 8
  private val WEIGHT_7 = 7
  private val WEIGHT_6 = 6
  private val WEIGHT_5 = 5
  private val WEIGHT_4 = 4
  private val WEIGHT_3 = 3
  private val WEIGHT_2 = 2

  private val weights =
    List(WEIGHT_9, WEIGHT_10, WEIGHT_11, WEIGHT_12, WEIGHT_13, WEIGHT_8, WEIGHT_7, WEIGHT_6, WEIGHT_5, WEIGHT_4, WEIGHT_3, WEIGHT_2)
  private val checkChars = List("A", "B", "C", "D", "E", "F", "G", "H", "X", "J", "K", "L", "M", "N", "Y", "P", "Q", "R", "S", "T", "Z", "V", "W")

  def validateRegimeRegNo(regime: Regime, regNum: String): Boolean = {
    validateRegime(regime, regNum) && validateRegNum(regNum)
  }

  def validateRegime(regime: Regime, regNum: String): Boolean = {
    if (!regime.equals(Regime.MGD)) {
      val ref = regNum.takeRight(REF_NO_LENGTH).toLong
      val regimeFromRegNo = {
        if (ref >= 3000000 && ref <= 3199999) {
          "GBD"
        } else if (ref >= 3200000 && ref <= 3399999) {
          "PBD"
        } else if (ref >= 3400000 && ref <= 3599999) {
          "RGD"
        } else {
          ""
        }
      }

      if (!regimeFromRegNo.trim.equals(regime.code.toUpperCase.trim)) {
        logger.info(s"validateRegime Regime does not match RegNum '$regime':'$regimeFromRegNo' '$regNum' '$ref'")
        false
      } else {
        logger.info(s"validateRegime Regime matches RegNum '$regime':'$regimeFromRegNo' '$regNum' '$ref'")
        true
      }
    } else {
      true
    }
  }

  def validateRegNum(regNum: String): Boolean = {
    if (!regNum.isBlank && regNum.length == 14) {
      if (regNum.toUpperCase.matches(regEx)) {
        val char3 = (regNum.toUpperCase.substring(2, 3).toCharArray.head.toInt - 32) * WEIGHT_9
        val sum = List.range(1, 11).map(x => weights(x) * regNum.substring(x + 2, x + 3).toInt).sum + char3
        val checkChar = checkChars(sum % 23)
        if (regNum.toUpperCase().substring(1, 2).equals(checkChar)) {
          true
        } else {
          logger.info(s"validateRegNum '$regNum' has invalid check character actual=${regNum.toUpperCase().substring(1, 2)} calculated=$checkChar")
          false
        }
      } else {
        logger.info(s"validateRegNum '$regNum' does not match regEx")
        false
      }
    } else {
      logger.info(s"validateRegNum '$regNum' is blank or not 14 chars")
      false
    }
  }
}
