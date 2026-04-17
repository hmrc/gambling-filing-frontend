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

package connectors

import models.MgdCertificate
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReadsInstances, HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class MgdCertificateConnector @Inject() (config: ServicesConfig, http: HttpClientV2)(implicit ec: ExecutionContext)
    extends HttpReadsInstances
    with Logging {

  private val baseUrl: String = config.baseUrl("gambling") + "/gambling/mgd"

  def getCertificate(mgdRegNumber: String)(implicit hc: HeaderCarrier): Future[MgdCertificate] = {
    http
      .get(url"$baseUrl/$mgdRegNumber/certificate")
      .execute[Either[UpstreamErrorResponse, HttpResponse]]
      .flatMap {
        case Right(response) if response.status == 200 =>
          Future.fromTry(Try(response.json.as[MgdCertificate]))
        case Left(upstream) =>
          Future.failed(upstream)
        case Right(response) =>
          Future.failed(
            UpstreamErrorResponse(
              s"Unexpected status while fetching MGD certificate: ${response.status}",
              response.status
            )
          )
      }
  }
}
