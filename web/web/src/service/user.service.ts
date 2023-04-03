import {Injectable} from '@angular/core';
import {Observable, of, ReplaySubject, Subject} from 'rxjs';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {User} from '../entity/user';
import {catchError, map, tap} from 'rxjs/operators';
import {AbstractControl, AsyncValidatorFn, ValidationErrors, ValidatorFn} from '@angular/forms';

import {WebsocketService} from './websocket.service';
import {WebSocketData} from "../entity/web-socket-data";


@Injectable({
  providedIn: 'root'
})
export class UserService {

  protected baseUrl = 'user';
  private currentLoginUser: User | null | undefined;
  /**
   * buffer 设置为 1
   * 只保留最新的登录用户
   */
  private currentLoginUser$ = new ReplaySubject<User>(1);
  /**
   * 绑定用户二维码
   */
  private onScanBindUserQrCode = new Subject<WebSocketData>();
  public onScanBindUserQrCode$ = this.onScanBindUserQrCode.asObservable() as Observable<WebSocketData>;

  /**
   * 登录二维码
   */
  private onScanLoginQrCode = new Subject<WebSocketData>();
  public onScanLoginQrCode$ = this.onScanLoginQrCode.asObservable() as Observable<WebSocketData>;

  constructor(protected httpClient: HttpClient,
              private websocketServer: WebsocketService) {
    this.websocketServer.autowiredUserService(this);
    // 订阅的时候需要有user打头
    this.websocketServer.register('/user/stomp/scanBindUserQrCode', this.onScanBindUserQrCode);
    this.websocketServer.register('/user/stomp/scanLoginQrCode', this.onScanLoginQrCode);

  }


  /**
   * 生成绑定的二维码
   */
  generateBindQrCode(): Observable<string> {
    return this.httpClient.get<string>(`${this.baseUrl}/generateBindQrCode`);
  }


  /**
   * 获取当前登录用户
   */
  getCurrentLoginUser$(): Observable<User> {
    return this.currentLoginUser$;
  }

  /**
   * 获取登录二维码
   */
  getLoginQrCode(): Observable<string> {
    return this.httpClient.get<string>(`${this.baseUrl}/getLoginQrCode/${this.websocketServer.uuid}`);
  }

  /**
   * 根据username获取角色.
   * @param username 用户名
   */
  getRolesByUsername(username: string): Observable<Array<number>> {
    const params = new HttpParams().append('username', username);
    return this.httpClient.get<Array<number>>(this.baseUrl + '/getRolesByUsername', {params});
  }


  /**
   * 请求当前登录用户
   */
  initCurrentLoginUser(callback?: () => void): Observable<User> {
    return new Observable<User>(subscriber => {
      this.httpClient.get<User>(`${this.baseUrl}/currentLoginUser`)
        .subscribe((user: User) => {
            this.setCurrentLoginUser(user);
            subscriber.next(user);
          }, error => {
            if (callback) {
              callback();
            }
            this.setCurrentLoginUser(undefined);
            subscriber.error(error);
          },
          () => {
            if (callback) {
              callback();
            }
            subscriber.complete();
          });
    });
  }


  logout(): Observable<void> {
    return this.httpClient.get<void>(`${this.baseUrl}/logout`).pipe(map(() => {
      this.setCurrentLoginUser(undefined);
    }));
  }

  login(user: {username: string, password: string}): Observable<User> {
    // 新建Headers，并添加认证信息
    let headers = new HttpHeaders();
    // 添加认证信息
    headers = headers.append('Authorization',
      'Basic ' + btoa(user.username + ':' + encodeURIComponent(user.password)));

    // 发起get请求并返回
    return this.httpClient.get<User>(`${this.baseUrl}/login`, {headers})
      .pipe(tap(data => this.setCurrentLoginUser(data)));
  }


  /**
   * 设置当前登录用户
   * @param user 登录用户
   */
  setCurrentLoginUser(user: User | undefined): void {
    this.currentLoginUser = user;
    this.currentLoginUser$.next(user);
  }



}
