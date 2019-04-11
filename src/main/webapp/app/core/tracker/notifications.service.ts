import { Injectable } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { Observable, Observer, Subscription } from 'rxjs';

import { CSRFService } from '../auth/csrf.service';
import { WindowRef } from './window.service';
import { AuthServerProvider } from '../auth/auth-jwt.service';

import * as SockJS from 'sockjs-client';
import * as Stomp from 'webstomp-client';

@Injectable({ providedIn: 'root' })
export class NotificationsService {
    stompClient = null;
    subscriber = null;
    connection: Promise<any>;
    connectedPromise: any;
    listener: Observable<any>;
    listenerObserver: Observer<any>;
    alreadyConnectedOnce = false;
    private subscription: Subscription;
    private isNotificationSupported: boolean;
    private notificationAllowed: boolean;
    notificationsSubscriber = null;

    connect() {
        if (this.connectedPromise === null) {
            this.connection = this.createConnection();
        }
        const loc = this.$window.nativeWindow.location;
        let url;
        url = '//' + loc.host + loc.pathname + 'websocket/tracker';
        const authToken = this.authServerProvider.getToken();
        if (authToken) {
            url += '?access_token=' + authToken;
        }
        const socket = new SockJS(url);
        this.stompClient = Stomp.over(socket);
        const headers = {};
        this.stompClient.connect(headers, () => {
            this.connectedPromise('success');
            this.connectedPromise = null;
            if (!this.alreadyConnectedOnce) {
                this.subscription = this.router.events.subscribe();
                this.alreadyConnectedOnce = true;
            }
        });
    }

    constructor(
        private router: Router,
        private authServerProvider: AuthServerProvider,
        private $window: WindowRef,
        // tslint:disable-next-line: no-unused-variable
        private csrfService: CSRFService
    ) {
        this.connection = this.createConnection();
        this.listener = this.createListener();
    }

    private createListener(): Observable<any> {
        return new Observable(observer => {
            this.listenerObserver = observer;
        });
    }

    private createConnection(): Promise<any> {
        return new Promise((resolve, reject) => (this.connectedPromise = resolve));
    }

    subscribe() {
        this.connection.then(() => {
            this.subscriber = this.stompClient.subscribe('/user/queue/notify', data => {
                this.listenerObserver.next(JSON.parse(data.body));
            });
        });
    }

    receive() {
        return this.listener;
    }

    subscribeToNotifications() {
        this.isNotificationSupported = 'Notification' in window;
        if (this.isNotificationSupported) {
            Notification.requestPermission(status => {
                if (status === 'granted') {
                    this.notificationAllowed = true;
                }
            });
        }
    }

    createNotification(title: string, options?: any) {
        return new Observable(obs => {
            if (this.notificationAllowed) {
                const _notify = new Notification(title, options);
                _notify.onshow = function(e) {
                    return obs.next({
                        notification: _notify,
                        event: e
                    });
                };
                _notify.onclick = function(e) {
                    return obs.next({
                        notification: _notify,
                        event: e
                    });
                };
                _notify.onerror = function(e) {
                    return obs.error({
                        notification: _notify,
                        event: e
                    });
                };
                _notify.onclose = function() {
                    return obs.complete();
                };
            }
        });
    }
}
