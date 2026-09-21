<script setup lang="ts">
import { ref, watch, onUnmounted, nextTick } from 'vue'
// @ts-ignore svgaplayerweb 无官方类型
import SVGA from 'svgaplayerweb'

export interface GiftFxPayload {
  giftId?: number
  giftName?: string
  icon?: string
  senderName?: string
  count?: number
  combo?: number
  level?: number
}

const props = defineProps<{
  gift: GiftFxPayload | null
}>()

const visibleBanner = ref(false)
const visibleStage = ref(false)
const current = ref<GiftFxPayload | null>(null)
const svgaHost = ref<HTMLDivElement | null>(null)
let bannerTimer: any = null
let stageTimer: any = null
let svgaPlayer: any = null
let svgaParser: any = null

/** 礼物名 → SVGA 文件（放在 public/gifts/effects/） */
const SVGA_MAP: Record<string, string> = {
  '点赞': '/gifts/effects/666.svga',
  '紫色玫瑰': '/gifts/effects/紫色玫瑰.svga',
  '爱心气球': '/gifts/effects/爱心气球.svga',
  '钻石': '/gifts/effects/钻石.svga',
  '红色跑车': '/gifts/effects/红色跑车.svga',
  '爱心熊熊': '/gifts/effects/爱心熊熊.svga',
  'LV包': '/gifts/effects/LV包.svga',
  '超级火箭': '/gifts/effects/超级火箭.svga',
  '紫色城堡': '/gifts/effects/紫色城堡.svga',
  '爱心直升机': '/gifts/effects/爱心直升机.svga',
  '幸福马车': '/gifts/effects/幸福马车.svga',
  '天马': '/gifts/effects/天马.svga',
  '爱的漂流瓶': '/gifts/effects/爱的漂流瓶.svga',
  '财神到': '/gifts/effects/财神到.svga',
  '666': '/gifts/effects/666.svga'
}

const svgaOf = (name?: string) => {
  if (!name) return ''
  if (SVGA_MAP[name]) return SVGA_MAP[name]
  // 模糊匹配
  const key = Object.keys(SVGA_MAP).find(k => name.includes(k) || k.includes(name))
  return key ? SVGA_MAP[key] : ''
}

const destroySvga = () => {
  try {
    svgaPlayer?.stopAnimation?.()
    svgaPlayer?.clear?.()
  } catch { /* ignore */ }
  svgaPlayer = null
  svgaParser = null
  if (svgaHost.value) {
    svgaHost.value.innerHTML = ''
  }
}

const playSvga = async (url: string) => {
  destroySvga()
  await nextTick()
  if (!svgaHost.value) return
  // 中文文件名需 URL 编码，否则 XHR 拉流可能 404
  const encoded = url
    .split('/')
    .map((seg, i) => (i === 0 ? seg : encodeURIComponent(seg)))
    .join('/')
  try {
    svgaParser = new SVGA.Parser()
    svgaPlayer = new SVGA.Player(svgaHost.value)
    // 素材为手机端竖屏(约 9:16)，交给播放器按比例适配居中，避免桌面端拉伸/偏移
    svgaPlayer.setContentMode('AspectFit')
    svgaPlayer.setClipsToBounds(true)
    svgaParser.load(encoded, (videoItem: any) => {
      try {
        svgaPlayer.setVideoItem(videoItem)
        svgaPlayer.loops = 1
        svgaPlayer.clearsAfterStop = true
        svgaPlayer.startAnimation()
      } catch (e) {
        console.warn('SVGA play fail', e)
      }
    }, (err: any) => {
      console.warn('SVGA load fail', url, err)
    })
  } catch (e) {
    console.warn('SVGA init fail', e)
  }
}

const clearTimers = () => {
  if (bannerTimer) clearTimeout(bannerTimer)
  if (stageTimer) clearTimeout(stageTimer)
  bannerTimer = null
  stageTimer = null
}

watch(
  () => props.gift,
  (g) => {
    if (!g) return
    current.value = g
    const lv = g.level || 1
    visibleBanner.value = true
    visibleStage.value = lv >= 2

    const url = svgaOf(g.giftName)
    // 有 SVGA 的礼物即使一星也播小动画层（爱心气球等）
    if (url && lv >= 2) {
      visibleStage.value = true
    }

    const stageMs = lv >= 3 ? 4200 : lv === 2 || url ? 3000 : 0
    const bannerMs = Math.max(stageMs, lv === 1 ? 2400 : 3200)

    clearTimers()
    bannerTimer = setTimeout(() => {
      visibleBanner.value = false
    }, bannerMs)

    if (visibleStage.value) {
      if (stageTimer) clearTimeout(stageTimer)
      stageTimer = setTimeout(() => {
        visibleStage.value = false
        destroySvga()
      }, stageMs || 3000)
      if (url) {
        playSvga(url)
      }
    } else {
      destroySvga()
    }
  },
  { deep: true }
)

onUnmounted(() => {
  clearTimers()
  destroySvga()
})
</script>

<template>
  <!-- 一星：B 站式底部浮层横幅 -->
  <transition name="banner">
    <div
        v-if="visibleBanner && current"
        class="bili-banner"
        :class="'lv' + (current?.level || 1)"
    >
      <img v-if="current.icon" :src="current.icon" class="bb-icon" alt="" />
      <div class="bb-text">
        <span class="bb-name">{{ current.senderName }}</span>
        <span class="bb-action">送出</span>
        <span class="bb-gift">{{ current.giftName }}</span>
        <span v-if="(current.count || 1) > 1" class="bb-count">×{{ current.count }}</span>
      </div>
      <span v-if="(current.combo || 1) > 1" class="bb-combo">{{ current.combo }}<i>连击</i></span>
    </div>
  </transition>

  <!-- 二星/三星：B 站桌面端全宽舞台 -->
  <div
      v-show="visibleStage && !!current"
      class="bili-stage"
      :class="'lv' + (current?.level || 1)"
  >
    <div class="bs-mask"></div>
    <!-- 两侧氛围装饰：光晕 + 漂浮粒子 + 星光，填充横屏留白 -->
    <div class="bs-decor">
      <div class="bs-glow bs-glow-left"></div>
      <div class="bs-glow bs-glow-right"></div>
      <div class="bs-glow bs-glow-center"></div>
      <div class="bs-particles">
        <i></i><i></i><i></i><i></i><i></i><i></i>
        <i></i><i></i><i></i><i></i><i></i><i></i>
      </div>
      <div class="bs-stars">
        <i>✦</i><i>✦</i><i>✦</i><i>✦</i><i>✦</i>
      </div>
    </div>
    <div class="bs-frame">
      <div ref="svgaHost" class="svga-host"></div>
    </div>
    <div v-if="current" class="bs-meta">
      <div class="bs-line">
        <span class="bs-user">{{ current.senderName }}</span>
        <span class="bs-verb">送出</span>
        <span class="bs-gift">{{ current.giftName }}</span>
        <span v-if="(current.count || 1) > 1" class="bs-count">×{{ current.count }}</span>
      </div>
      <div v-if="(current.combo || 1) > 1" class="bs-combo">
        <em>{{ current.combo }}</em><span>连击</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ===== B 站风格：底部送礼浮条 ===== */
.bili-banner {
  position: fixed;
  left: 50%;
  bottom: 112px;
  transform: translateX(-50%);
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  border-radius: 20px;
  pointer-events: none;
  max-width: min(92vw, 560px);
  background: linear-gradient(90deg, rgba(251, 114, 153, 0.92), rgba(255, 107, 129, 0.88));
  color: #fff;
  font-weight: 600;
  box-shadow: 0 6px 24px rgba(251, 114, 153, 0.35);
  backdrop-filter: blur(6px);
  border: 1px solid rgba(255, 255, 255, 0.18);
}
.bb-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: contain;
  background: rgba(255, 255, 255, 0.18);
  padding: 2px;
  flex-shrink: 0;
  animation: bbPop 0.35s cubic-bezier(0.2, 0.9, 0.3, 1.3);
}
.bb-text {
  display: flex;
  align-items: baseline;
  gap: 6px;
  flex-wrap: wrap;
  min-width: 0;
  font-size: 14px;
  line-height: 1.3;
}
.bb-name {
  font-weight: 700;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.bb-action { opacity: 0.9; font-weight: 500; }
.bb-gift { font-weight: 700; }
.bb-count { opacity: 0.95; font-weight: 500; }
.bb-combo {
  margin-left: 4px;
  background: rgba(0, 0, 0, 0.22);
  border-radius: 14px;
  padding: 2px 10px;
  font-size: 13px;
  font-style: normal;
  display: inline-flex;
  align-items: baseline;
  gap: 2px;
}
.bb-combo i {
  font-style: normal;
  font-size: 11px;
  opacity: 0.9;
  font-weight: 500;
}
.bili-banner.lv2 {
  background: linear-gradient(90deg, rgba(255, 154, 91, 0.95), rgba(255, 181, 71, 0.9));
  box-shadow: 0 6px 24px rgba(255, 154, 91, 0.35);
}
.bili-banner.lv3 {
  background: linear-gradient(90deg, rgba(241, 196, 15, 0.95), rgba(255, 107, 107, 0.92));
  box-shadow: 0 8px 28px rgba(255, 200, 80, 0.4);
  border-color: rgba(255, 255, 255, 0.3);
}

.banner-enter-active { animation: bannerIn 0.32s cubic-bezier(0.2, 0.9, 0.3, 1.2); }
.banner-leave-active { animation: bannerOut 0.22s ease forwards; }

/* ===== B 站桌面端：全宽大舞台 ===== */
.bili-stage {
  position: fixed;
  inset: 0;
  z-index: 1100;
  pointer-events: none;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.bs-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(0, 0, 0, 0.55) 0%,
    rgba(0, 0, 0, 0.72) 45%,
    rgba(0, 0, 0, 0.82) 100%
  );
  animation: maskIn 0.35s ease;
}
/* 画框：竖屏动画按高度铺开、水平居中，适配桌面端 */
.bs-frame {
  position: relative;
  z-index: 1;
  width: min(92vw, 1100px);
  height: min(82vh, 680px);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: stagePop 0.45s cubic-bezier(0.2, 0.9, 0.3, 1.12);
}
.svga-host {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
}
/* 播放器通过 canvas 的 transform 自行居中缩放，这里只保证 block 布局即可 */
.svga-host :deep(canvas) {
  display: block;
  margin: 0 auto;
}

/* ===== 两侧氛围装饰：光晕 + 漂浮粒子 + 星光 ===== */
.bs-decor {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}
.bs-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(70px);
  will-change: opacity;
}
.bs-glow-left {
  left: -10vw;
  top: 10%;
  width: 44vw;
  height: 44vw;
  background: radial-gradient(circle, rgba(251, 114, 153, 0.5), transparent 70%);
  animation: glowPulse 3.6s ease-in-out infinite;
}
.bs-glow-right {
  right: -10vw;
  bottom: 6%;
  width: 44vw;
  height: 44vw;
  background: radial-gradient(circle, rgba(255, 196, 80, 0.45), transparent 70%);
  animation: glowPulse 3.6s ease-in-out infinite reverse;
}
.bs-glow-center {
  left: 50%;
  top: 50%;
  width: 58vw;
  height: 70vh;
  transform: translate(-50%, -50%);
  background: radial-gradient(ellipse, rgba(255, 150, 180, 0.26), transparent 72%);
  animation: glowPulse 3.6s ease-in-out infinite;
}
.bs-particles {
  position: absolute;
  inset: 0;
}
.bs-particles i {
  position: absolute;
  bottom: -14px;
  border-radius: 50%;
  background: radial-gradient(circle, #fff, rgba(255, 255, 255, 0) 72%);
  box-shadow: 0 0 8px 3px rgba(255, 255, 255, 0.45);
  opacity: 0;
  animation: particleRise linear infinite;
}
.bs-stars {
  position: absolute;
  inset: 0;
}
.bs-stars i {
  position: absolute;
  font-style: normal;
  color: #fff;
  text-shadow: 0 0 10px rgba(255, 220, 150, 0.9);
  animation: starTwinkle 2.4s ease-in-out infinite;
}

.bs-particles i:nth-child(1)  { left: 6%;  width: 7px; height: 7px; animation-duration: 6.2s; animation-delay: 0s; }
.bs-particles i:nth-child(2)  { left: 14%; width: 5px; height: 5px; animation-duration: 7.4s; animation-delay: 1.1s; }
.bs-particles i:nth-child(3)  { left: 22%; width: 9px; height: 9px; animation-duration: 6.8s; animation-delay: 2.3s; background: radial-gradient(circle, #ffd6e7, rgba(255, 214, 231, 0) 72%); box-shadow: 0 0 10px 4px rgba(251, 114, 153, 0.5); }
.bs-particles i:nth-child(4)  { left: 31%; width: 6px; height: 6px; animation-duration: 8.1s; animation-delay: 0.6s; }
.bs-particles i:nth-child(5)  { left: 40%; width: 5px; height: 5px; animation-duration: 6.5s; animation-delay: 3.2s; }
.bs-particles i:nth-child(6)  { left: 50%; width: 8px; height: 8px; animation-duration: 7.7s; animation-delay: 1.8s; background: radial-gradient(circle, #fff3c4, rgba(255, 243, 196, 0) 72%); box-shadow: 0 0 10px 4px rgba(255, 196, 80, 0.5); }
.bs-particles i:nth-child(7)  { left: 58%; width: 5px; height: 5px; animation-duration: 6.9s; animation-delay: 0.3s; }
.bs-particles i:nth-child(8)  { left: 66%; width: 9px; height: 9px; animation-duration: 8.4s; animation-delay: 2.8s; background: radial-gradient(circle, #ffd6e7, rgba(255, 214, 231, 0) 72%); box-shadow: 0 0 10px 4px rgba(251, 114, 153, 0.5); }
.bs-particles i:nth-child(9)  { left: 74%; width: 6px; height: 6px; animation-duration: 7.1s; animation-delay: 1.5s; }
.bs-particles i:nth-child(10) { left: 82%; width: 5px; height: 5px; animation-duration: 6.3s; animation-delay: 3.6s; }
.bs-particles i:nth-child(11) { left: 90%; width: 8px; height: 8px; animation-duration: 7.9s; animation-delay: 0.9s; background: radial-gradient(circle, #fff3c4, rgba(255, 243, 196, 0) 72%); box-shadow: 0 0 10px 4px rgba(255, 196, 80, 0.5); }
.bs-particles i:nth-child(12) { left: 96%; width: 6px; height: 6px; animation-duration: 8.7s; animation-delay: 2.1s; }

.bs-stars i { font-size: 22px; }
.bs-stars i:nth-child(1) { left: 10%; top: 22%; animation-delay: 0s; }
.bs-stars i:nth-child(2) { left: 88%; top: 30%; animation-delay: 0.5s; font-size: 16px; }
.bs-stars i:nth-child(3) { left: 18%; bottom: 26%; animation-delay: 1.1s; font-size: 18px; }
.bs-stars i:nth-child(4) { left: 80%; bottom: 22%; animation-delay: 0.7s; font-size: 20px; }
.bs-stars i:nth-child(5) { left: 92%; top: 60%; animation-delay: 1.4s; font-size: 15px; }
/* 底部送礼信息条（B 站 lower-third） */
.bs-meta {
  position: absolute;
  left: 50%;
  bottom: 10%;
  transform: translateX(-50%);
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  pointer-events: none;
  animation: metaIn 0.4s cubic-bezier(0.2, 0.9, 0.3, 1.15);
  width: min(92vw, 720px);
}
.bs-line {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;
  padding: 12px 28px;
  border-radius: 999px;
  background: rgba(18, 18, 24, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(10px);
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.35);
}
.bs-user {
  color: #fb7299;
  font-weight: 700;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.bs-verb { opacity: 0.75; font-weight: 500; }
.bs-gift { font-weight: 700; color: #fff; }
.bs-count { opacity: 0.85; font-weight: 500; }
.bs-combo {
  display: flex;
  align-items: baseline;
  gap: 6px;
  color: #fff;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.5);
}
.bs-combo em {
  font-style: normal;
  font-size: 36px;
  font-weight: 800;
  color: #fb7299;
  line-height: 1;
  letter-spacing: -1px;
}
.bs-combo span {
  font-size: 16px;
  font-weight: 600;
  opacity: 0.9;
}

@keyframes bannerIn {
  from { opacity: 0; transform: translateX(-50%) translateY(22px); }
  to { opacity: 1; transform: translateX(-50%) translateY(0); }
}
@keyframes bannerOut {
  to { opacity: 0; transform: translateX(-50%) translateY(10px); }
}
@keyframes bbPop {
  from { transform: scale(0.45); }
  to { transform: scale(1); }
}
@keyframes metaIn {
  from { opacity: 0; transform: translateX(-50%) translateY(16px); }
  to { opacity: 1; transform: translateX(-50%) translateY(0); }
}
@keyframes maskIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes stagePop {
  from { opacity: 0; transform: scale(0.94); }
  to { opacity: 1; transform: scale(1); }
}
@keyframes glowPulse {
  0%, 100% { opacity: 0.55; }
  50% { opacity: 1; }
}
@keyframes particleRise {
  0%   { transform: translateY(0) translateX(0) scale(1); opacity: 0; }
  8%   { opacity: 0.9; }
  70%  { opacity: 0.7; }
  100% { transform: translateY(-105vh) translateX(40px) scale(0.35); opacity: 0; }
}
@keyframes starTwinkle {
  0%, 100% { opacity: 0.25; transform: scale(0.8) rotate(0deg); }
  50% { opacity: 1; transform: scale(1.15) rotate(20deg); }
}
</style>
