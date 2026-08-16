import { ref, watch, onMounted } from 'vue'

export function useCountUp(source: () => number | undefined, duration = 900) {
  const display = ref(0)
  let raf = 0

  function animate(from: number, to: number) {
    cancelAnimationFrame(raf)
    const start = performance.now()
    const step = (now: number) => {
      const t = Math.min((now - start) / duration, 1)
      const eased = 1 - Math.pow(1 - t, 3)
      display.value = Math.round(from + (to - from) * eased)
      if (t < 1) raf = requestAnimationFrame(step)
    }
    raf = requestAnimationFrame(step)
  }

  onMounted(() => {
    watch(
      source,
      (val) => {
        if (typeof val === 'number') animate(display.value, val)
      },
      { immediate: true }
    )
  })

  return display
}
