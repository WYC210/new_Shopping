<template>
  <span>{{ displayValue }}</span>
</template>

<script>
export default {
  name: 'CountTo',
  props: {
    startVal: {
      type: Number,
      default: 0
    },
    endVal: {
      type: Number,
      required: true
    },
    duration: {
      type: Number,
      default: 3000
    },
    decimals: {
      type: Number,
      default: 0
    },
    autoplay: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      displayValue: this.formatValue(this.startVal),
      localStartVal: this.startVal,
      printVal: null,
      paused: false,
      localDuration: this.duration,
      startTime: null,
      timestamp: null,
      remaining: null,
      rAF: null
    }
  },
  computed: {
    countDown() {
      return this.startVal > this.endVal
    }
  },
  watch: {
    startVal() {
      this.localStartVal = this.startVal
      this.displayValue = this.formatValue(this.startVal)
    },
    endVal() {
      this.start()
    }
  },
  mounted() {
    if (this.autoplay) {
      this.start()
    }
    this.$emit('mounted')
  },
  methods: {
    start() {
      this.localStartVal = this.startVal
      this.startTime = null
      this.localDuration = this.duration
      this.paused = false
      this.rAF = requestAnimationFrame(this.count)
    },
    count(timestamp) {
      if (!this.startTime) this.startTime = timestamp
      this.timestamp = timestamp
      const progress = timestamp - this.startTime
      this.remaining = this.localDuration - progress

      if (this.countDown) {
        this.printVal = this.localStartVal - (this.localStartVal - this.endVal) * (progress / this.localDuration)
      } else {
        this.printVal = this.localStartVal + (this.endVal - this.localStartVal) * (progress / this.localDuration)
      }
      
      if (this.countDown) {
        this.printVal = this.printVal < this.endVal ? this.endVal : this.printVal
      } else {
        this.printVal = this.printVal > this.endVal ? this.endVal : this.printVal
      }

      this.displayValue = this.formatValue(this.printVal)
      
      if (progress < this.localDuration) {
        this.rAF = requestAnimationFrame(this.count)
      } else {
        this.$emit('callback')
      }
    },
    formatValue(value) {
      return Number(value).toFixed(this.decimals)
    }
  },
  beforeUnmount() {
    cancelAnimationFrame(this.rAF)
  }
}
</script> 