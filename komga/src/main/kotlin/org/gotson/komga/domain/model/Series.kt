package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

data class Series(
  val name: String,
  val url: URL,
  val fileLastModified: LocalDateTime,

  val id: String = TsidCreator.getTsidString256(),
  val libraryId: String = "",
  val deleted: Boolean = false,

  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = LocalDateTime.now()
) : Auditable() {

  fun path(): Path = Paths.get(this.url.toURI())
}
