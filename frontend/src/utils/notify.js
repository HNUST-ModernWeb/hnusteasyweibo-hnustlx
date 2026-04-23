let container = null

function ensureContainer() {
  if (container) return container
  container = document.createElement('div')
  container.style.position = 'fixed'
  container.style.top = '20px'
  container.style.left = '50%'
  container.style.transform = 'translateX(-50%)'
  container.style.zIndex = '9999'
  container.style.display = 'flex'
  container.style.flexDirection = 'column'
  container.style.gap = '10px'
  container.style.pointerEvents = 'none'
  document.body.appendChild(container)
  return container
}

function show(message, type = 'info', duration = 1800) {
  const root = ensureContainer()
  const el = document.createElement('div')

  const palette = {
    success: { bg: '#ecfdf3', border: '#34d399', color: '#065f46' },
    error: { bg: '#fef2f2', border: '#f87171', color: '#991b1b' },
    info: { bg: '#eff6ff', border: '#60a5fa', color: '#1e40af' }
  }
  const p = palette[type] || palette.info

  el.textContent = message
  el.style.minWidth = '220px'
  el.style.maxWidth = '420px'
  el.style.padding = '10px 14px'
  el.style.borderRadius = '12px'
  el.style.border = `1px solid ${p.border}`
  el.style.background = p.bg
  el.style.color = p.color
  el.style.fontSize = '14px'
  el.style.fontWeight = '600'
  el.style.boxShadow = '0 8px 24px rgba(0,0,0,0.08)'
  el.style.pointerEvents = 'auto'
  el.style.opacity = '0'
  el.style.transform = 'translateY(-8px)'
  el.style.transition = 'all 0.2s ease'

  root.appendChild(el)
  requestAnimationFrame(() => {
    el.style.opacity = '1'
    el.style.transform = 'translateY(0)'
  })

  setTimeout(() => {
    el.style.opacity = '0'
    el.style.transform = 'translateY(-8px)'
    setTimeout(() => {
      if (el.parentNode) el.parentNode.removeChild(el)
    }, 200)
  }, duration)
}

export const notify = {
  success: (msg, duration) => show(msg, 'success', duration),
  error: (msg, duration) => show(msg, 'error', duration),
  info: (msg, duration) => show(msg, 'info', duration)
}
