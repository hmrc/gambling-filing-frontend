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

package repositories

import models.{MgdCertificate, MgdCertificateDAO}
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Indexes, ReplaceOptions}
import org.mongodb.scala.bson.conversions.Bson
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MgdCertificateCacheRepository @Inject() (
  mongoComponent: MongoComponent,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[MgdCertificateDAO](
      collectionName = "mgd-certificate-cache",
      mongoComponent = mongoComponent,
      domainFormat   = MgdCertificateDAO.format,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions().name("lastUpdatedIdx").expireAfter(3600, TimeUnit.SECONDS)
        )
      )
    ) {

  private def byId(id: String): Bson = Filters.equal("_id", id)

  def cacheCertificate(cert: MgdCertificate): Future[Boolean] = {
    val dao = MgdCertificateDAO(cert.mgdRegNumber, Instant.now(clock), cert)
    collection
      .replaceOne(byId(cert.mgdRegNumber), dao, ReplaceOptions().upsert(true))
      .toFuture()
      .map(_ => true)
  }

  def getCertificate(id: String): Future[Option[MgdCertificate]] =
    collection.find(byId(id)).headOption().map(_.map(_.certificate))
}
