<template>
  <div v-if="!$_.isEmpty(book)">
    <toolbar-sticky>
      <!--   Action menu   -->
      <book-actions-menu v-if="book"
                         :book="book"
      />

      <v-btn icon @click="editBook" v-if="isAdmin">
        <v-icon>mdi-pencil</v-icon>
      </v-btn>

      <v-spacer/>

      <!--   Context notification for navigation   -->
      <v-alert
        v-if="contextReadList"
        type="info"
        text
        dense
        border="right"
        class="mb-0"
      >{{ $t('browse_book.navigation_within_readlist') }}: {{ contextName }}
      </v-alert>

      <!--   Navigate to previous book   -->
      <v-btn
        icon
        :disabled="$_.isEmpty(siblingPrevious)"
        :to="{ name: 'browse-book', params: { bookId: previousId }, query: { context: context.origin, contextId: context.id}  }"
      >
        <v-icon v-if="$vuetify.rtl">mdi-chevron-right</v-icon>
        <v-icon v-else>mdi-chevron-left</v-icon>
      </v-btn>

      <!--   List of all books in context (series/readlist) for navigation   -->
      <v-menu bottom
              offset-y
              :max-height="$vuetify.breakpoint.height * .7"
              :max-width="250"
      >
        <template v-slot:activator="{ on }">
          <v-btn icon v-on="on">
            <v-icon>mdi-menu</v-icon>
          </v-btn>
        </template>

        <v-list
          flat
        >
          <v-list-item-group color="primary">
            <v-list-item
              v-for="(book, i) in siblings"
              :key="i"
              :to="{ name: 'browse-book', params: { bookId: book.id }, query: { context: context.origin, contextId: context.id} }"
            >
              <v-list-item-title class="text-wrap text-body-2">{{ book.metadata.number }} - {{ book.metadata.title }}
              </v-list-item-title>
            </v-list-item>
          </v-list-item-group>
        </v-list>
      </v-menu>

      <!--   Navigate to next book   -->
      <v-btn
        icon
        :disabled="$_.isEmpty(siblingNext)"
        :to="{ name: 'browse-book', params: { bookId: nextId }, query: { context: context.origin, contextId: context.id}  }"
      >
        <v-icon v-if="$vuetify.rtl">mdi-chevron-left</v-icon>
        <v-icon v-else>mdi-chevron-right</v-icon>
      </v-btn>
    </toolbar-sticky>

    <v-container fluid class="px-6">
      <v-row>
        <v-col cols="4" sm="4" md="auto" lg="auto" xl="auto">
          <item-card
            v-if="book.hasOwnProperty('id')"
            width="212"
            :item="book"
            thumbnail-only
            no-link
            :action-menu="false"
          ></item-card>
        </v-col>

        <v-col cols="8">
          <v-row>
            <v-col class="py-1">
              <router-link :to="{name:'browse-series', params: {seriesId: book.seriesId}}" class="link-underline">
                <span class="text-h5" v-if="!$_.isEmpty(series)">{{ series.metadata.title }}</span>
              </router-link>
            </v-col>
          </v-row>
          <v-row>
            <v-col class="py-1">
              <div class="text-h6">{{ book.metadata.title }}</div>
            </v-col>
          </v-row>

          <v-row class="text-body-2">
            <v-col>
              <span class="mx-3">{{ book.metadata.number }}</span>
            </v-col>
            <v-col cols="auto" v-if="book.metadata.releaseDate">
              {{ book.metadata.releaseDate | moment('MMMM DD, YYYY') }}
            </v-col>
          </v-row>

          <v-divider/>

          <v-row class="mt-3">
            <v-col>
              <read-more>{{ book.metadata.summary }}</read-more>
            </v-col>
          </v-row>

          <v-divider v-if="book.metadata.authors.length > 0"/>

          <v-row class="align-center text-body-2"
                 v-for="role in authorRoles"
                 :key="role"
          >
            <v-col cols="6" sm="4" md="3" class="py-1 text-uppercase">{{ $t(`author_roles.${role}`) }}</v-col>
            <v-col cols="6" sm="8" md="9" class="py-1">
              <vue-horizontal>
                <template v-slot:btn-prev>
                  <v-btn icon small>
                    <v-icon>mdi-chevron-left</v-icon>
                  </v-btn>
                </template>

                <template v-slot:btn-next>
                  <v-btn icon small>
                    <v-icon>mdi-chevron-right</v-icon>
                  </v-btn>
                </template>
                <v-chip v-for="(name, i) in authorsByRole[role]"
                        :key="i"
                        :class="$vuetify.rtl ? 'ml-2' : 'mr-2'"
                        :title="name"
                        :to="{name:'browse-series', params: {seriesId: book.seriesId }, query: {[role]: name}}"
                        label
                        small
                        outlined
                        link
                >{{ name }}
                </v-chip>
              </vue-horizontal>
            </v-col>
          </v-row>

          <v-row v-if="book.metadata.tags.length > 0" class="align-center text-body-2">
            <v-col cols="6" sm="4" md="3" class="py-1">TAGS</v-col>
            <v-col cols="6" sm="8" md="9" class="py-1 text-capitalize">
              <vue-horizontal>
                <template v-slot:btn-prev>
                  <v-btn icon small>
                    <v-icon>mdi-chevron-left</v-icon>
                  </v-btn>
                </template>

                <template v-slot:btn-next>
                  <v-btn icon small>
                    <v-icon>mdi-chevron-right</v-icon>
                  </v-btn>
                </template>
                <v-chip v-for="(t, i) in book.metadata.tags"
                        :key="i"
                        :class="$vuetify.rtl ? 'ml-2' : 'mr-2'"
                        :title="t"
                        :to="{name:'browse-series', params: {seriesId: book.seriesId}, query: {tag: t}}"
                        label
                        small
                        outlined
                        link
                >{{ t }}
                </v-chip>
              </vue-horizontal>
            </v-col>
          </v-row>

          <v-row v-if="$vuetify.breakpoint.name !== 'xs'">
            <v-col>
              <read-lists-expansion-panels :read-lists="readLists"/>
            </v-col>
          </v-row>
        </v-col>
      </v-row>

      <v-row v-if="$vuetify.breakpoint.name === 'xs'">
        <v-col class="pt-0 py-1">
          <read-lists-expansion-panels :read-lists="readLists"/>
        </v-col>
      </v-row>

      <v-row align="center">
        <v-col cols="auto">
          <v-btn icon
                 :title="$t('browse_book.download_file')"
                 class="pb-1"
                 :disabled="!canDownload"
                 :href="fileUrl">
            <v-icon>mdi-file-download</v-icon>
          </v-btn>
        </v-col>
        <v-col cols="auto">
          <v-btn icon
                 color="accent"
                 :title="$t('browse_book.read_book')"
                 class="pb-1"
                 :to="{name: 'read-book', params: { bookId: bookId}, query: { context: context.origin, contextId: context.id}}"
                 :disabled="book.media.status !== 'READY' || !canReadPages"
          >
            <v-icon>mdi-book-open-page-variant</v-icon>
          </v-btn>
        </v-col>
        <v-col cols="auto">
          <v-icon class="mx-2 pb-1">mdi-book-open</v-icon>
          <span class="text-body-2">{{ book.media.pagesCount }} {{ $t('common.pages') }}</span>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="3" md="2" lg="2" xl="1" class="text-body-2">{{ $t('browse_book.size') }}</v-col>
        <v-col cols="9" class="text-body-2">{{ book.size }}</v-col>
      </v-row>

      <v-row v-if="book.media.comment">
        <v-col cols="3" md="2" lg="2" xl="1" class="text-body-2">{{ $t('browse_book.comment') }}</v-col>
        <v-col cols="9" class="text-body-2">
          <span class="error--text font-weight-bold">{{ book.media.comment }}</span>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="3" md="2" lg="2" xl="1" class="text-body-2">{{ $t('browse_book.format') }}</v-col>
        <v-col cols="9" class="text-body-2">
          <span>{{ format.type }}</span>
        </v-col>
      </v-row>

      <v-row align="center">
        <v-col cols="3" md="2" lg="2" xl="1" class="text-body-2">{{ $t('browse_book.file') }}</v-col>
        <v-col cols="9" class="text-body-2">{{ book.url }}</v-col>
      </v-row>

    </v-container>

  </div>
</template>

<script lang="ts">
import BookActionsMenu from '@/components/menus/BookActionsMenu.vue'
import ItemCard from '@/components/ItemCard.vue'
import ToolbarSticky from '@/components/bars/ToolbarSticky.vue'
import {groupAuthorsByRole} from '@/functions/authors'
import {getBookFormatFromMediaType} from '@/functions/book-format'
import {getReadProgress, getReadProgressPercentage} from '@/functions/book-progress'
import {getBookTitleCompact} from '@/functions/book-title'
import {bookFileUrl, bookThumbnailUrl} from '@/functions/urls'
import {ReadStatus} from '@/types/enum-books'
import {BOOK_CHANGED, LIBRARY_DELETED} from '@/types/events'
import Vue from 'vue'
import ReadListsExpansionPanels from '@/components/ReadListsExpansionPanels.vue'
import {BookDto, BookFormat} from '@/types/komga-books'
import {Context, ContextOrigin} from '@/types/context'
import {SeriesDto} from "@/types/komga-series";
import ReadMore from "@/components/ReadMore.vue";
import VueHorizontal from "vue-horizontal";
import {authorRoles} from "@/types/author-roles";

export default Vue.extend({
  name: 'BrowseBook',
  components: {ReadMore, ToolbarSticky, ItemCard, BookActionsMenu, ReadListsExpansionPanels, VueHorizontal},
  data: () => {
    return {
      authorRoles,
      book: {} as BookDto,
      series: {} as SeriesDto,
      context: {} as Context,
      contextName: '',
      siblings: [] as BookDto[],
      siblingPrevious: {} as BookDto,
      siblingNext: {} as BookDto,
      readLists: [] as ReadListDto[],
    }
  },
  async created() {
    this.loadBook(this.bookId)
    this.$eventHub.$on(BOOK_CHANGED, this.reloadBook)
    this.$eventHub.$on(LIBRARY_DELETED, this.libraryDeleted)
  },
  beforeDestroy() {
    this.$eventHub.$off(BOOK_CHANGED, this.reloadBook)
    this.$eventHub.$off(LIBRARY_DELETED, this.libraryDeleted)
  },
  props: {
    bookId: {
      type: String,
      required: true,
    },
  },
  async beforeRouteUpdate(to, from, next) {
    if (to.params.bookId !== from.params.bookId) {
      this.loadBook(to.params.bookId)
    }

    next()
  },
  computed: {
    isAdmin(): boolean {
      return this.$store.getters.meAdmin
    },
    canReadPages(): boolean {
      return this.$store.getters.mePageStreaming
    },
    canDownload(): boolean {
      return this.$store.getters.meFileDownload
    },
    thumbnailUrl(): string {
      return bookThumbnailUrl(this.bookId)
    },
    fileUrl(): string {
      return bookFileUrl(this.bookId)
    },
    format(): BookFormat {
      return getBookFormatFromMediaType(this.book.media.mediaType)
    },
    authorsByRole (): any {
      return groupAuthorsByRole(this.book.metadata.authors)
    },
    isRead(): boolean {
      return getReadProgress(this.book) === ReadStatus.READ
    },
    isUnread(): boolean {
      return getReadProgress(this.book) === ReadStatus.UNREAD
    },
    isInProgress(): boolean {
      return getReadProgress(this.book) === ReadStatus.IN_PROGRESS
    },
    readProgressPercentage(): number {
      return getReadProgressPercentage(this.book)
    },
    previousId(): string {
      return this.siblingPrevious?.id?.toString() || '0'
    },
    nextId(): string {
      return this.siblingNext?.id?.toString() || '0'
    },
    contextReadList(): boolean {
      return this.context.origin === ContextOrigin.READLIST
    },
  },
  methods: {
    libraryDeleted(event: EventLibraryDeleted) {
      if (event.id === this.book.libraryId) {
        this.$router.push({name: 'home'})
      }
    },
    reloadBook(event: EventBookChanged) {
      if (event.id === this.bookId) this.loadBook(this.bookId)
    },
    async loadBook(bookId: string) {
      this.book = await this.$komgaBooks.getBook(bookId)
      this.series = await this.$komgaSeries.getOneSeries(this.book.seriesId)

      // parse query params to get context and contextId
      if (this.$route.query.contextId && this.$route.query.context
        && Object.values(ContextOrigin).includes(this.$route.query.context as ContextOrigin)) {
        this.context = {
          origin: this.$route.query.context as ContextOrigin,
          id: this.$route.query.contextId as string,
        }
        this.book.context = this.context
        this.contextName = (await (this.$komgaReadLists.getOneReadList(this.context.id))).name
      }

      // Get siblings depending on origin
      if (this?.context.origin === ContextOrigin.READLIST) {
        this.siblings = (await this.$komgaReadLists.getBooks(this.context.id, {unpaged: true} as PageRequest)).content
      } else {
        this.siblings = (await this.$komgaSeries.getBooks(this.book.seriesId, {unpaged: true} as PageRequest)).content
      }

      this.readLists = await this.$komgaBooks.getReadLists(this.bookId)

      if (this.$_.has(this.book, 'metadata.title')) {
        document.title = `Komga - ${getBookTitleCompact(this.book.metadata.title, this.series.metadata.title)}`
      }

      try {
        if (this?.context.origin === ContextOrigin.READLIST) {
          this.siblingNext = await this.$komgaReadLists.getBookSiblingNext(this.context.id, bookId)
        } else {
          this.siblingNext = await this.$komgaBooks.getBookSiblingNext(bookId)
        }
      } catch (e) {
        this.siblingNext = {} as BookDto
      }
      try {
        if (this?.context.origin === ContextOrigin.READLIST) {
          this.siblingPrevious = await this.$komgaReadLists.getBookSiblingPrevious(this.context.id, bookId)
        } else {
          this.siblingPrevious = await this.$komgaBooks.getBookSiblingPrevious(bookId)
        }
      } catch (e) {
        this.siblingPrevious = {} as BookDto
      }
    },
    analyze() {
      this.$komgaBooks.analyzeBook(this.book)
    },
    refreshMetadata() {
      this.$komgaBooks.refreshMetadata(this.book)
    },
    editBook() {
      this.$store.dispatch('dialogUpdateBooks', this.book)
    },
  },
})
</script>

<style scoped>
</style>
