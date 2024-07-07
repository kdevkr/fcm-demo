import {getCurrentMessaging} from "./config/firebase.js";
import {onMessage} from "firebase/messaging";
import {onBackgroundMessage} from "firebase/messaging/sw";

self.addEventListener('install', (e) => {
    console.log("installed SW! and skipWaiting...", e);
    self.skipWaiting();
});

self.addEventListener('activate', (e) => {
    console.log('activate and clients claim', e)
    e.waitUntil(self.clients.claim());
});

// NOTE: Receive both Foreground and Background messages.
self.addEventListener('push', (e) => {
    const payload = e.data.json();
    console.log('[sw.js] push ', payload);

    showNotification(payload, e);
})

const messaging = getCurrentMessaging();
if (messaging) {
    onMessage(messaging, (payload) => {
        // NOTE: The onMessage in the Firebase SDK is not delivered within the service worker.
        console.log('[sw.js] Received message', payload);
    })

    // NOTE: You don't need to rely on the Firebase SDK if you're using the Service Worker Push event.
    onBackgroundMessage(messaging, (payload) => {
        console.log('[sw.js] Received background message ', payload);

        showNotification(payload);
    });
}

const showNotification = (payload, e) => {
    // Customize notification here
    const notificationTitle = payload.data?.title || payload.notification?.title;
    const notificationOptions = {
        body: payload.data?.body || payload.notification?.body,
        image: payload.data?.image || payload.notification?.image,
        icon: '/pwa-512x512.png',
        tag: payload.fcmMessageId
    };

    if (Notification.permission === 'granted') {
        if (e) {
            e.waitUntil(self.registration.showNotification(notificationTitle, notificationOptions));
        } else {
            self.registration.showNotification(notificationTitle, notificationOptions).then();
        }
    }
}
