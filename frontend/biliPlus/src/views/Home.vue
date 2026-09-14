<script setup lang="ts">
import LunboTu from "@/views/home/Main/LunboTu.vue";
import Categry from "@/views/home/Main/Categry.vue";
import Video from "@/views/home/Main/Video/Video.vue";
import Footer from "@/views/home/Footer.vue";
import { ref } from 'vue'

const categoryId = ref<number | null>(null)

const handleCategoryChange = (id: number | null) => {
  categoryId.value = id
}
</script>

<template>
  <div class="home-page">
    <LunboTu></LunboTu>
    <Categry @change="handleCategoryChange"></Categry>
    <Video :category-id="categoryId"></Video>
    <Footer></Footer>
  </div>
</template>

<style scoped>
.home-page {
  background: var(--paper);
  min-height: calc(100vh - 60px);
}
</style>
