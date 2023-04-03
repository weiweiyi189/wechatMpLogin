import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {UserService} from "../service/user.service";
import {XAuthTokenInterceptor} from "../interceptor/x-auth-token.interceptor";
import {WebsocketService} from "../service/websocket.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = 'web';
  private token: string | undefined;

  constructor(private router: Router,
              private userService: UserService,
              private webSocketService: WebsocketService) {
  }

  ngOnInit(): void {
    if (this.router && this.router.url && !this.router.url.startsWith(`/login`)) {
      this.userService.initCurrentLoginUser(() => {
      }).subscribe({
        error: () =>
          this.router.navigateByUrl('/login').then()
      });
    }

    // 登录用户改变时，重新将wsUuid与xAuthToken绑定
    this.userService.getCurrentLoginUser$()
      .subscribe(() => {
        const token = XAuthTokenInterceptor.getToken();
        if (token && this.token !== token) {
          this.token = token;
          this.webSocketService.send('/ws/bind', XAuthTokenInterceptor.getToken());
        }
      });
  }
}
