package org.gotson.komga.infrastructure.jooq

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.infrastructure.language.toIndexedMap
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest
class ReadListDaoTest(
  @Autowired private val readListDao: ReadListDao,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val libraryRepository: LibraryRepository
) {

  private val library = makeLibrary()
  private val library2 = makeLibrary("library2")

  @BeforeAll
  fun setup() {
    libraryRepository.insert(library)
    libraryRepository.insert(library2)
  }

  @AfterEach
  fun deleteSeries() {
    readListDao.deleteAll()
    bookRepository.deleteAll()
    seriesRepository.deleteAll()
  }

  @AfterAll
  fun tearDown() {
    libraryRepository.deleteAll()
  }

  @Test
  fun `given read list with books when inserting then it is persisted`() {
    // given
    val series = makeSeries("Series", library.id)
    seriesRepository.insert(series)
    val books = (1..10).map { makeBook("Book $it", libraryId = library.id, seriesId = series.id) }
    books.forEach { bookRepository.insert(it) }

    val readList = ReadList(
      name = "MyReadList",
      bookIds = books.map { it.id }.toIndexedMap()
    )

    // when
    val now = LocalDateTime.now()

    readListDao.insert(readList)
    val created = readListDao.findByIdOrNull(readList.id)!!

    // then
    assertThat(created.name).isEqualTo(readList.name)
    assertThat(created.createdDate)
      .isEqualTo(created.lastModifiedDate)
      .isCloseTo(now, offset)
    assertThat(created.bookIds.values).containsExactlyElementsOf(books.map { it.id })
  }

  @Test
  fun `given read list with updated books when updating then it is persisted`() {
    // given
    val series = makeSeries("Series", library.id)
    seriesRepository.insert(series)
    val books = (1..10).map { makeBook("Book $it", libraryId = library.id, seriesId = series.id) }
    books.forEach { bookRepository.insert(it) }

    val readList = ReadList(
      name = "MyReadList",
      bookIds = books.map { it.id }.toIndexedMap()
    )

    readListDao.insert(readList)

    // when
    val updatedReadList = readList.copy(
      name = "UpdatedReadList",
      bookIds = readList.bookIds.values.take(5).toIndexedMap()
    )

    val now = LocalDateTime.now()
    readListDao.update(updatedReadList)
    val updated = readListDao.findByIdOrNull(updatedReadList.id)!!

    // then
    assertThat(updated.name).isEqualTo(updatedReadList.name)
    assertThat(updated.createdDate).isNotEqualTo(updated.lastModifiedDate)
    assertThat(updated.lastModifiedDate).isCloseTo(now, offset)
    assertThat(updated.bookIds.values)
      .hasSize(5)
      .containsExactlyElementsOf(books.map { it.id }.take(5))
  }

  @Test
  fun `given read lists with books when removing one book from all then it is removed from all`() {
    // given
    val series = makeSeries("Series", library.id)
    seriesRepository.insert(series)
    val books = (1..10).map { makeBook("Book $it", libraryId = library.id, seriesId = series.id) }
    books.forEach { bookRepository.insert(it) }

    val readList1 = ReadList(
      name = "MyReadList",
      bookIds = books.map { it.id }.toIndexedMap()
    )
    readListDao.insert(readList1)

    val readList2 = ReadList(
      name = "MyReadList",
      bookIds = books.map { it.id }.take(5).toIndexedMap()
    )
    readListDao.insert(readList2)

    // when
    readListDao.removeBookFromAll(books.first().id)

    // then
    val rl1 = readListDao.findByIdOrNull(readList1.id)!!
    assertThat(rl1.bookIds.values)
      .hasSize(9)
      .doesNotContain(books.first().id)

    val col2 = readListDao.findByIdOrNull(readList2.id)!!
    assertThat(col2.bookIds.values)
      .hasSize(4)
      .doesNotContain(books.first().id)
  }

  @Test
  fun `given read lists spanning different libraries when finding by library then only matching collections are returned`() {
    // given
    val seriesLibrary1 = makeSeries("Series1", library.id).also { seriesRepository.insert(it) }
    val bookLibrary1 = makeBook("Book1", libraryId = library.id, seriesId = seriesLibrary1.id).also { bookRepository.insert(it) }
    val seriesLibrary2 = makeSeries("Series2", library2.id).also { seriesRepository.insert(it) }
    val bookLibrary2 = makeBook("Book2", libraryId = library2.id, seriesId = seriesLibrary2.id).also { bookRepository.insert(it) }

    readListDao.insert(
      ReadList(
        name = "readListLibrary1",
        bookIds = listOf(bookLibrary1.id).toIndexedMap()
      )
    )

    readListDao.insert(
      ReadList(
        name = "readListLibrary2",
        bookIds = listOf(bookLibrary2.id).toIndexedMap()
      )
    )

    readListDao.insert(
      ReadList(
        name = "readListLibraryBoth",
        bookIds = listOf(bookLibrary1.id, bookLibrary2.id).toIndexedMap()
      )
    )

    // when
    val foundLibrary1Filtered = readListDao.findAllByLibraries(listOf(library.id), listOf(library.id), pageable = Pageable.unpaged()).content
    val foundLibrary1Unfiltered = readListDao.findAllByLibraries(listOf(library.id), null, pageable = Pageable.unpaged()).content
    val foundLibrary2Filtered = readListDao.findAllByLibraries(listOf(library2.id), listOf(library2.id), pageable = Pageable.unpaged()).content
    val foundLibrary2Unfiltered = readListDao.findAllByLibraries(listOf(library2.id), null, pageable = Pageable.unpaged()).content
    val foundBothUnfiltered = readListDao.findAllByLibraries(listOf(library.id, library2.id), null, pageable = Pageable.unpaged()).content

    // then
    assertThat(foundLibrary1Filtered).hasSize(2)
    assertThat(foundLibrary1Filtered.map { it.name }).containsExactly("readListLibrary1", "readListLibraryBoth")
    with(foundLibrary1Filtered.find { it.name == "readListLibraryBoth" }!!) {
      assertThat(bookIds.values)
        .hasSize(1)
        .containsExactly(bookLibrary1.id)
      assertThat(filtered).isTrue()
    }

    assertThat(foundLibrary1Unfiltered).hasSize(2)
    assertThat(foundLibrary1Unfiltered.map { it.name }).containsExactly("readListLibrary1", "readListLibraryBoth")
    with(foundLibrary1Unfiltered.find { it.name == "readListLibraryBoth" }!!) {
      assertThat(bookIds.values)
        .hasSize(2)
        .containsExactly(bookLibrary1.id, bookLibrary2.id)
      assertThat(filtered).isFalse()
    }

    assertThat(foundLibrary2Filtered).hasSize(2)
    assertThat(foundLibrary2Filtered.map { it.name }).containsExactly("readListLibrary2", "readListLibraryBoth")
    with(foundLibrary2Filtered.find { it.name == "readListLibraryBoth" }!!) {
      assertThat(bookIds.values)
        .hasSize(1)
        .containsExactly(bookLibrary2.id)
      assertThat(filtered).isTrue()
    }

    assertThat(foundLibrary2Unfiltered).hasSize(2)
    assertThat(foundLibrary2Unfiltered.map { it.name }).containsExactly("readListLibrary2", "readListLibraryBoth")
    with(foundLibrary2Unfiltered.find { it.name == "readListLibraryBoth" }!!) {
      assertThat(bookIds.values)
        .hasSize(2)
        .containsExactly(bookLibrary1.id, bookLibrary2.id)
      assertThat(filtered).isFalse()
    }

    assertThat(foundBothUnfiltered).hasSize(3)
    assertThat(foundBothUnfiltered.map { it.name }).containsExactly("readListLibrary1", "readListLibrary2", "readListLibraryBoth")
    with(foundBothUnfiltered.find { it.name == "readListLibraryBoth" }!!) {
      assertThat(bookIds.values)
        .hasSize(2)
        .containsExactly(bookLibrary1.id, bookLibrary2.id)
      assertThat(filtered).isFalse()
    }
  }

  @Test
  fun `given read list with soft deleted book when finding deleted book by read list name then book is returned`() {
    // given
    val series = makeSeries("Series", library.id)
    seriesRepository.insert(series)
    val book1 = makeBook("Book", libraryId = library.id, seriesId = series.id)
    val book2 = makeBook("Book", libraryId = library.id, seriesId = series.id)
    bookRepository.insert(book1)
    bookRepository.insert(book2)
    bookRepository.softDeleteByBookIds(listOf(book1.id))

    val readList = ReadList(
      name = "MyReadList",
      bookIds = listOf(book1.id).toIndexedMap()
    )
    readListDao.insert(readList)

    // when
    val deletedBooks = readListDao.findDeletedBooksByName(readList.name)

    // then
    assertThat(deletedBooks).hasSize(1)
    assertThat(deletedBooks.values.first()).isEqualTo(book1.id)
  }

  @Test
  fun `given read list with soft deleted book when finding by id then book ids are empty`() {
    // given
    val series = makeSeries("Series", library.id)
    seriesRepository.insert(series)
    val book = makeBook("Book", libraryId = library.id, seriesId = series.id)
    bookRepository.insert(book)
    bookRepository.softDeleteByBookIds(listOf(book.id))

    val readList = ReadList(
      name = "MyReadList",
      bookIds = listOf(book.id).toIndexedMap()
    )
    readListDao.insert(readList)

    // when
    val deletedBooks = readListDao.findByIdOrNull(readList.id)

    // then
    assertThat(deletedBooks).isNotNull
    assertThat(deletedBooks!!.bookIds).isEmpty()
  }

  @Test
  fun `given read list with soft deleted book when searching then book ids are empty`() {
    // given
    val series = makeSeries("Series", library.id)
    seriesRepository.insert(series)
    val book = makeBook("Book", libraryId = library.id, seriesId = series.id)
    bookRepository.insert(book)
    bookRepository.softDeleteByBookIds(listOf(book.id))

    val readList = ReadList(
      name = "MyReadList",
      bookIds = listOf(book.id).toIndexedMap()
    )
    readListDao.insert(readList)

    // when
    val found = readListDao.findAll(readList.name, PageRequest.of(0, 20))

    // then
    assertThat(found).hasSize(1)
    assertThat(found.first().bookIds).isEmpty()
  }

  @Test
  fun `given read list with soft deleted book when searching with library id filter then book ids are empty`() {
    // given
    val series = makeSeries("Series", library.id)
    seriesRepository.insert(series)
    val book = makeBook("Book", libraryId = library.id, seriesId = series.id)
    bookRepository.insert(book)
    bookRepository.softDeleteByBookIds(listOf(book.id))

    val readList = ReadList(
      name = "MyReadList",
      bookIds = listOf(book.id).toIndexedMap()
    )
    readListDao.insert(readList)

    // when
    val found = readListDao.findByIdOrNull(readList.id, listOf(library.id))

    // then
    assertThat(found).isNotNull
    assertThat(found!!.bookIds).isEmpty()
  }

  @Test
  fun `given read list with soft deleted book when finding all by libraries then book ids are empty`() {
    // given
    val series = makeSeries("Series", library.id)
    seriesRepository.insert(series)
    val book = makeBook("Book", libraryId = library.id, seriesId = series.id)
    bookRepository.insert(book)
    bookRepository.softDeleteByBookIds(listOf(book.id))

    val readList = ReadList(
      name = "MyReadList",
      bookIds = listOf(book.id).toIndexedMap()
    )
    readListDao.insert(readList)

    // when
    val found = readListDao.findAllByLibraries(listOf(library.id), null, null, PageRequest.of(0, 20))

    // then
    assertThat(found).isNotNull
    assertThat(found.first().bookIds).isEmpty()
  }

  @Test
  fun `given read list with soft deleted book when finding all by book then empty list is returned`() {
    // given
    val series = makeSeries("Series", library.id)
    seriesRepository.insert(series)
    val book = makeBook("Book", libraryId = library.id, seriesId = series.id)
    bookRepository.insert(book)
    bookRepository.softDeleteByBookIds(listOf(book.id))

    val readList = ReadList(
      name = "MyReadList",
      bookIds = listOf(book.id).toIndexedMap()
    )
    readListDao.insert(readList)

    // when
    val found = readListDao.findAllByBook(book.id, null)

    // then
    assertThat(found).isEmpty()
  }

  @Test
  fun `given read list with soft deleted book when finding by name then then book ids are empty`() {
    // given
    val series = makeSeries("Series", library.id)
    seriesRepository.insert(series)
    val book = makeBook("Book", libraryId = library.id, seriesId = series.id)
    bookRepository.insert(book)
    bookRepository.softDeleteByBookIds(listOf(book.id))

    val readList = ReadList(
      name = "MyReadList",
      bookIds = listOf(book.id).toIndexedMap()
    )
    readListDao.insert(readList)

    // when
    val found = readListDao.findByNameOrNull(readList.name)

    // then
    assertThat(found).isNotNull
    assertThat(found!!.bookIds).isEmpty()
  }
}
