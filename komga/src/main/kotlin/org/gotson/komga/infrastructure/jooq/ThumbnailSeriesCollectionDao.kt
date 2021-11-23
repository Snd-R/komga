package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.ThumbnailSeriesCollection
import org.gotson.komga.domain.persistence.ThumbnailSeriesCollectionRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.ThumbnailCollectionRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ThumbnailSeriesCollectionDao(
  private val dsl: DSLContext
) : ThumbnailSeriesCollectionRepository {
  private val tc = Tables.THUMBNAIL_COLLECTION

  override fun findByIdOrNull(thumbnailId: String): ThumbnailSeriesCollection? =
    dsl.selectFrom(tc)
      .where(tc.ID.eq(thumbnailId))
      .fetchOneInto(tc)
      ?.toDomain()

  override fun findSelectedByCollectionIdOrNull(collectionId: String): ThumbnailSeriesCollection? =
    dsl.selectFrom(tc)
      .where(tc.COLLECTION_ID.eq(collectionId))
      .and(tc.SELECTED.isTrue)
      .limit(1)
      .fetchInto(tc)
      .map { it.toDomain() }
      .firstOrNull()

  override fun findAllByCollectionId(collectionId: String): Collection<ThumbnailSeriesCollection> =
    dsl.selectFrom(tc)
      .where(tc.COLLECTION_ID.eq(collectionId))
      .fetchInto(tc)
      .map { it.toDomain() }

  override fun insert(thumbnail: ThumbnailSeriesCollection) {
    dsl.insertInto(tc)
      .set(tc.ID, thumbnail.id)
      .set(tc.COLLECTION_ID, thumbnail.collectionId)
      .set(tc.THUMBNAIL, thumbnail.thumbnail)
      .set(tc.SELECTED, thumbnail.selected)
      .set(tc.TYPE, thumbnail.type.toString())
      .execute()
  }

  override fun update(thumbnail: ThumbnailSeriesCollection) {
    dsl.update(tc)
      .set(tc.COLLECTION_ID, thumbnail.collectionId)
      .set(tc.THUMBNAIL, thumbnail.thumbnail)
      .set(tc.SELECTED, thumbnail.selected)
      .set(tc.TYPE, thumbnail.type.toString())
      .where(tc.ID.eq(thumbnail.id))
      .execute()
  }

  @Transactional
  override fun markSelected(thumbnail: ThumbnailSeriesCollection) {
    dsl.update(tc)
      .set(tc.SELECTED, false)
      .where(tc.COLLECTION_ID.eq(thumbnail.collectionId))
      .and(tc.ID.ne(thumbnail.id))
      .execute()

    dsl.update(tc)
      .set(tc.SELECTED, true)
      .where(tc.COLLECTION_ID.eq(thumbnail.collectionId))
      .and(tc.ID.eq(thumbnail.id))
      .execute()
  }

  override fun delete(thumbnailBookId: String) {
    dsl.deleteFrom(tc).where(tc.ID.eq(thumbnailBookId)).execute()
  }

  private fun ThumbnailCollectionRecord.toDomain() =
    ThumbnailSeriesCollection(
      thumbnail = thumbnail,
      selected = selected,
      type = ThumbnailSeriesCollection.Type.valueOf(type),
      id = id,
      collectionId = collectionId,
      createdDate = createdDate,
      lastModifiedDate = lastModifiedDate
    )
}
