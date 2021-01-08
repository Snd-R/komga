package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesSearch
import java.net.URL

interface SeriesRepository {
  fun findAll(): Collection<Series>
  fun findByIdOrNull(seriesId: String): Series?
  fun findByLibraryId(libraryId: String): Collection<Series>
  fun findByLibraryIdAndUrlNotIn(libraryId: String, urls: Collection<URL>): Collection<Series>
  fun existsByLibraryIdAndUrl(libraryId: String, url: URL): Boolean

  fun findByLibraryIdAndUrlIncludeDeleted(libraryId: String, url: URL): Series?
  fun findByLibraryIdAndHashesInIncludeDeleted(libraryId: String, hashes: Collection<String>): Collection<Series>
  fun findByHashesInIncludeDeleted(hashes: Collection<String>): Collection<Series>

  fun findAllDeleted(): Collection<String>
  fun findAll(search: SeriesSearch): Collection<Series>

  fun getLibraryId(seriesId: String): String?

  fun insert(series: Series)
  fun update(series: Series)

  fun delete(seriesId: String)
  fun deleteAll()
  fun deleteAll(seriesIds: Collection<String>)
  fun softDeleteAll(seriesIds: Collection<String>)

  fun count(): Long
}
