<script setup lang="ts">

import LunboTu from "@/views/home/Main/LunboTu.vue";
import Categry from "@/views/home/Main/Categry.vue";
import Video from "@/views/home/Main/Video/Video.vue";
import { ref } from 'vue'

const categoryId = ref<number | null>(null)

const handleCategoryChange = (id: number | null) => {
  categoryId.value = id
}
</script>

<template>

  <LunboTu></LunboTu>
  <Categry @change="handleCategoryChange"></Categry>
  <Video :category-id="categoryId"></Video>

</template>

<style scoped>

</style>
