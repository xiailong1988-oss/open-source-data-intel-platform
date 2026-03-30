/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_AMAP_KEY?: string
  readonly VITE_AMAP_SECURITY_CODE?: string
  readonly VITE_BMAP_AK?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
