import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import mkcert from 'vite-plugin-mkcert'
import {VitePWA} from 'vite-plugin-pwa'

// https://vitejs.dev/config/
export default defineConfig({
    server: {
        host: '0.0.0.0',
    },
    plugins: [
        vue(),
        mkcert(),
        VitePWA({
            includeAssets: ['favicon.ico', 'apple-touch-icon.png'],
            registerType: 'autoUpdate',
            injectRegister: null,
            strategies: "injectManifest",
            srcDir: "src",
            filename: "sw.js",
            workbox: {
                clientsClaim: true,
                skipWaiting: true
            },
            devOptions: {
                enabled: true,
                type: 'module'
            },
            manifest: {
                name: 'FCM demo',
                short_name: 'FCM',
                description: '',
                theme_color: '#ffffff',
                icons: [
                    {
                        src: 'pwa-64x64.png',
                        sizes: '64x64',
                        type: 'image/png'
                    },
                    {
                        src: 'pwa-192x192.png',
                        sizes: '192x192',
                        type: 'image/png'
                    },
                    {
                        src: 'pwa-512x512.png',
                        sizes: '512x512',
                        type: 'image/png'
                    },
                    {
                        src: 'maskable-icon-512x512.png',
                        sizes: '512x512',
                        type: 'image/png',
                        purpose: 'maskable'
                    }
                ]
            }
        })
    ],
})
