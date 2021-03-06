<template>
  <div id="searchbox">
    <v-text-field v-model="search"
                  solo
                  hide-details
                  clearable
                  prepend-inner-icon="mdi-magnify"
                  :label="$t('search.search')"
                  :loading="loading"
                  @click:clear="clear"
                  @keydown.esc="clear"
                  @keydown.enter="searchDetails"
    />
    <v-menu nudge-bottom="57"
            nudge-right="52"
            attach="#searchbox"
            v-model="showResults"
            :max-height="$vuetify.breakpoint.height * .9"
            :min-width="$vuetify.breakpoint.mdAndUp ? $vuetify.breakpoint.width * .4 : $vuetify.breakpoint.width * .8"
    >
      <v-list>
        <v-list-item
          v-if="series.length === 0 && books.length === 0 && collections.length === 0 && readLists.length === 0">
          {{ $t('searchbox.no_results') }}
        </v-list-item>

        <template v-if="series.length !== 0">
          <v-subheader class="text-uppercase">{{ $t('common.series') }}</v-subheader>
          <v-list-item v-for="item in series"
                       :key="item.id"
                       link
                       :to="{name: 'browse-series', params: {seriesId: item.id}}"
          >
            <v-img :src="seriesThumbnailUrl(item.id)"
                   height="50"
                   max-width="35"
                   class="my-1 mx-3"
            />
            <v-list-item-content>
              <v-list-item-title v-text="item.metadata.title"/>
            </v-list-item-content>
          </v-list-item>
        </template>

        <template v-if="books.length !== 0">
          <v-subheader class="text-uppercase">{{ $t('common.books') }}</v-subheader>
          <v-list-item v-for="item in books"
                       :key="item.id"
                       link
                       :to="{name: 'browse-book', params: {bookId: item.id}}"
          >
            <v-img :src="bookThumbnailUrl(item.id)"
                   height="50"
                   max-width="35"
                   class="my-1 mx-3"
            />
            <v-list-item-content>
              <v-list-item-title v-text="item.metadata.title"/>
            </v-list-item-content>
          </v-list-item>
        </template>

        <template v-if="collections.length !== 0">
          <v-subheader class="text-uppercase">{{ $t('common.collections') }}</v-subheader>
          <v-list-item v-for="item in collections"
                       :key="item.id"
                       link
                       :to="{name: 'browse-collection', params: {collectionId: item.id}}"
          >
            <v-img :src="collectionThumbnailUrl(item.id)"
                   height="50"
                   max-width="35"
                   class="my-1 mx-3"
            />
            <v-list-item-content>
              <v-list-item-title v-text="item.name"/>
            </v-list-item-content>
          </v-list-item>
        </template>

        <template v-if="readLists.length !== 0">
          <v-subheader class="text-uppercase">{{ $t('common.readlists') }}</v-subheader>
          <v-list-item v-for="item in readLists"
                       :key="item.id"
                       link
                       :to="{name: 'browse-readlist', params: {readListId: item.id}}"
          >
            <v-img :src="readListThumbnailUrl(item.id)"
                   height="50"
                   max-width="35"
                   class="my-1 mx-3"
            />
            <v-list-item-content>
              <v-list-item-title v-text="item.name"/>
            </v-list-item-content>
          </v-list-item>
        </template>

      </v-list>
    </v-menu>
  </div>
</template>

<script lang="ts">
import {bookThumbnailUrl, collectionThumbnailUrl, readListThumbnailUrl, seriesThumbnailUrl} from '@/functions/urls'
import {debounce} from 'lodash'
import Vue from 'vue'
import {BookDto} from '@/types/komga-books'
import {SeriesDto} from "@/types/komga-series";

export default Vue.extend({
  name: 'SearchBox',
  data: function () {
    return {
      search: null,
      showResults: false,
      loading: false,
      series: [] as SeriesDto[],
      books: [] as BookDto[],
      collections: [] as CollectionDto[],
      readLists: [] as ReadListDto[],
      pageSize: 10,
    }
  },
  watch: {
    search(val) {
      this.searchItems(val)
    },
    showResults(val) {
      !val && this.clear()
    },
  },
  methods: {
    searchItems: debounce(async function (this: any, query: string) {
      if (query) {
        this.loading = true
        this.series = (await this.$komgaSeries.getSeries(undefined, {size: this.pageSize}, query)).content
        this.books = (await this.$komgaBooks.getBooks(undefined, {size: this.pageSize}, query)).content
        this.collections = (await this.$komgaCollections.getCollections(undefined, {size: this.pageSize}, query)).content
        this.readLists = (await this.$komgaReadLists.getReadLists(undefined, {size: this.pageSize}, query)).content
        this.showResults = true
        this.loading = false
      } else {
        this.clear()
      }
    }, 500),
    clear() {
      this.search = null
      this.showResults = false
      this.series = []
      this.books = []
      this.collections = []
      this.readLists = []
    },
    searchDetails() {
      const s = this.search
      this.clear()
      this.$router.push({name: 'search', query: {q: s}}).catch(e => {
      })
    },
    seriesThumbnailUrl(seriesId: string): string {
      return seriesThumbnailUrl(seriesId)
    },
    bookThumbnailUrl(bookId: string): string {
      return bookThumbnailUrl(bookId)
    },
    collectionThumbnailUrl(collectionId: string): string {
      return collectionThumbnailUrl(collectionId)
    },
    readListThumbnailUrl(readListId: string): string {
      return readListThumbnailUrl(readListId)
    },
  },
})
</script>

<style scoped>

</style>
