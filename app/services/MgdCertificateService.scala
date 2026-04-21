/*
 * Copyright 2026 HM Revenue & Customs
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

package services

import connectors.MgdCertificateConnector
import models.MgdCertificate
import play.api.mvc.Request
import repositories.MgdCertificateCacheRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MgdCertificateService @Inject() (
  connector: MgdCertificateConnector,
  repo: MgdCertificateCacheRepository
)(implicit ec: ExecutionContext) {

  def retrieveCertificate(mgdRegNumber: String)(implicit hc: HeaderCarrier, request: Request[?]): Future[MgdCertificate] = {
    repo.getCertificate(mgdRegNumber).flatMap {
      case Some(cert) =>
        Future.successful(cert)
      case None =>
        connector.getCertificate(mgdRegNumber).flatMap { cert =>
          repo.cacheCertificate(cert).map(_ => cert)
        }
    }
  }
}
