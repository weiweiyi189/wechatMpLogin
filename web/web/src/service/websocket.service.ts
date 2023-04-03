import {Injectable} from '@angular/core';
import {BehaviorSubject, Subject} from 'rxjs';
import {filter, first, share, tap} from 'rxjs/operators';
import {UserService} from "./user.service";
declare var require: any;
const { v4: uuid } = require('uuid');

declare var SockJS: any;
declare var Stomp: any;

/**
 * websocket
 */
@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  private readonly AUTH_KEY = 'ws-auth-token';
  public readonly uuid = window.sessionStorage.getItem(this.AUTH_KEY) ? window.sessionStorage.getItem(this.AUTH_KEY) : uuid();
  private destinationPrefix = '/app';
  private observables = {} as Record<string, Subject<any>>;
  private userService: UserService | undefined;

  /**
   * stomp客户端
   * 缓存一个，后面用于多播
   */
  private stompClientSubject = new BehaviorSubject<any>(null);
  private stompClient$ = this.stompClientSubject.asObservable().pipe(share());

  public constructor() {
    window.sessionStorage.setItem(this.AUTH_KEY, this.uuid);

    // 模块组件onInit，异步执行，也可以防止cycle
    setTimeout(() => {
      this.onInit();
    });
  }

  private onInit() {
    this.userService?.getCurrentLoginUser$().pipe(first())
      .subscribe(() => {
        // websocket连接时进行uuid认证
        const socketUrl = `/api/websocket?${this.AUTH_KEY}=${this.uuid}`;
        const socket = new SockJS(socketUrl);
        const stompClient = Stomp.over(socket);
        stompClient.connect({
          "ws-auth-token": this.uuid
        }, (frame: any) => {
          // 添加个uuid, 用于后续进行debug，看是否为单例.
          // todo: 断开连接时重新进行连接
          stompClient.id = uuid();
          this.stompClientSubject.next(stompClient);
        });
      });
  }

  /**
   * 注入userService, 防止cycle
   * @param userService 用户
   */
  autowiredUserService(userService: UserService) {
    this.userService = userService;
  }

  /**
   * 注册路由
   * @param router 路由
   * @param subject 后台回发webSocket时发送数据流
   */
  register<T>(router: string, subject: Subject<T>): void {
    if (this.observables[router]) {
      throw new Error('未能够重复注册关键字' + router);
    }
    this.observables[router] = subject;
    this.stompClient$.pipe(filter(v => v !== null), first()).subscribe(stompClient => {
      stompClient.subscribe(this.getUrl(router), (data: any) => {
        subject.next(data);
      });
    });
  }

  /**
   * 发送
   * @param url url
   * @param message 发送的消息
   */
  public send(url: string, message: any) {
    if (message === null || message === undefined) {
      return;
    }

    this.stompClient$.pipe(filter(v => v !== null), first())
      .subscribe(stompClient => {
        let data = typeof message === 'object' ? JSON.stringify(message) : message;
        stompClient.send(this.destinationPrefix + this.getUrl(url), {
          "ws-auth-token": this.uuid
        }, data);
      });
  }

  /**
   * 获取前缀，兼容以'/'或是不以'/'打头
   * @param url 原url
   */
  private getUrl(url: string): string {
    if (url.startsWith('/')) {
      return `${url}`;
    } else {
      return `/${url}`;
    }
  }
}
