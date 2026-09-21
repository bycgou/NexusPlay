<template>
  <div>
    <input type="file" accept="video/*" @change="handleFileSelect" />
    <button @click="startUpload">开始上传</button>
    <div>上传进度：{{ progress }}%</div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import SparkMD5 from 'spark-md5';
import axios from 'axios';

const file = ref(null); // 选中的视频文件
const progress = ref(0); // 上传进度
const chunkSize = 5 * 1024 * 1024; // 分片大小：5MB
const fileMd5 = ref(''); // 文件唯一标识（MD5）

// 选择视频文件
const handleFileSelect = (e) => {
  file.value = e.target.files[0];
  if (file.value) {
    calculateFileMd5(); // 计算文件MD5（用于唯一标识）
  }
};

// 计算文件MD5（大文件分片计算，避免卡顿）
const calculateFileMd5 = () => {
  const fileReader = new FileReader();
  const spark = new SparkMD5.ArrayBuffer();
  const chunkList = []; // 分片列表
  let offset = 0; // 读取偏移量

  // 分片读取文件并计算MD5
  const loadNextChunk = () => {
    const end = Math.min(offset + chunkSize, file.value.size);
    fileReader.readAsArrayBuffer(file.value.slice(offset, end));
    fileReader.onload = (e) => {
      spark.append(e.target.result);
      offset = end;
      if (offset < file.value.size) {
        loadNextChunk(); // 继续读取下一分片
      } else {
        fileMd5.value = spark.end(); // 最终MD5值
        console.log('文件MD5：', fileMd5.value);
      }
    };
  };

  loadNextChunk();
};

// 开始分片上传
const startUpload = async () => {
  if (!file.value || !fileMd5.value) return;

  const totalChunks = Math.ceil(file.value.size / chunkSize); // 总片数
  let uploadedChunks = 0; // 已上传片数

  // 遍历所有分片，逐个上传
  for (let chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
    const start = chunkIndex * chunkSize;
    const end = Math.min(start + chunkSize, file.value.size);
    const chunk = file.value.slice(start, end); // 当前分片二进制数据

    // 构建分片上传的JSON数据（含分片信息+二进制转Base64）
    const formData = new FormData(); // 注意：大文件分片不适合用JSON直接传二进制，此处用FormData配合JSON字段
    formData.append('fileInfo', JSON.stringify({
      md5: fileMd5.value, // 文件唯一标识
      chunkIndex: chunkIndex, // 当前分片索引
      totalChunks: totalChunks, // 总片数
      fileName: file.value.name, // 原文件名
      fileType: file.value.type // 文件类型
    }));
    formData.append('chunkData', chunk); // 分片二进制数据（直接传二进制，而非Base64，避免体积膨胀）

    // 上传分片
    try {
      await axios.post('/api/upload/chunk', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: (e) => {
          // 计算当前分片的上传进度，累计到总进度
          const chunkProgress = (e.loaded / e.total) * 100;
          progress.value = Math.floor((uploadedChunks * 100 + chunkProgress) / totalChunks);
        }
      });
      uploadedChunks++;
    } catch (err) {
      console.error(`分片${chunkIndex}上传失败：`, err);
      return; // 失败则终止上传
    }
  }

  // 所有分片上传完成，请求合并
  const mergeRes = await axios.post('/api/upload/merge', {
    md5: fileMd5.value,
    fileName: file.value.name,
    totalChunks: totalChunks
  });

  if (mergeRes.data.success) {
    progress.value = 100;
    console.log('文件上传完成，地址：', mergeRes.data.fileUrl);
    // 此处可将 fileUrl 填入业务JSON（如视频信息），发送给后端
    // 例：const videoData = { title: 'xxx', videoUrl: mergeRes.data.fileUrl, ... };
  }
};
</script>

<style scoped>

</style>