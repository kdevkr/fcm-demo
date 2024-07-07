import {getApps, initializeApp} from "firebase/app";
import {getToken, getMessaging} from "firebase/messaging";
import firebaseJson from './firebase.json';

const firebaseConfig = {...firebaseJson};
const vapidKey = firebaseConfig.vapidKey;

let messaging;
let swRegistration;

const getCurrentMessaging = () => {
    if (messaging) {
        return messaging;
    }

    let firebaseApp;
    if (getApps().length < 1) {
        firebaseApp = initializeApp(firebaseConfig);
    } else {
        firebaseApp = getApps().pop();
    }

    messaging = getMessaging(firebaseApp);
    return messaging;
}

const getCurrentToken = async () => {
    const messaging = getCurrentMessaging();
    if (Notification.permission !== 'granted' || !messaging || !swRegistration) {
        return null;
    }
    return await getToken(messaging, {vapidKey, serviceWorkerRegistration: swRegistration});
}

const init = () => {
    if (getApps().length > 0) {
        return;
    }

    if ('serviceWorker' in navigator) {
        const sw = import.meta.env.MODE === 'production' ? '/sw.js' : '/dev-sw.js?dev-sw'
        navigator.serviceWorker.register(
            sw,
            {type: import.meta.env.MODE === 'production' ? 'classic' : 'module'}
        ).then(async registration => {
            swRegistration = registration;
            console.log('register service worker', sw, registration)

            if (Notification.permission === 'granted') {
                const firebaseApp = initializeApp(firebaseConfig);
                messaging = getMessaging(firebaseApp)

                const currentToken = await getCurrentToken();
                console.log('// TODO: Save the token as a server when the PWA app is activated.')
                console.log('currentToken', currentToken)

                document.addEventListener("visibilitychange", () => {
                    const state = document.visibilityState;
                    if (state === "visible") {
                        console.log('// TODO: The PWA app has been activated in the background. Please update the token to the server.')
                    }
                });
            }
        })
    }
}

export {firebaseConfig, vapidKey, init, getCurrentMessaging};