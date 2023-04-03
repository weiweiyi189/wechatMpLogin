import {Component, OnInit, SecurityContext} from '@angular/core';
import {WebsocketService} from "../../service/websocket.service";
import {UserService} from "../../service/user.service";
import {filter, first} from "rxjs/operators";
import {User} from "../../entity/user";
import {Router} from "@angular/router";
import {DomSanitizer, SafeResourceUrl, SafeUrl} from "@angular/platform-browser";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  user: User | undefined;
  isShowQrCode = false;
  qrCodeSrc: SafeUrl | undefined;

  constructor(private userService: UserService,
              private router:Router,
              private sanitizer: DomSanitizer) { }

  ngOnInit(): void {
    this.userService.getCurrentLoginUser$()
      .pipe(filter(v => v !== null && v !== undefined))
      .subscribe((data:User) => {
        this.setUser(data);
      });
  }

  setUser(user: User): void {
    this.user = user;
  }

  onBindWeChat() {
    this.userService.generateBindQrCode()
      .subscribe(src => {
       this.qrCodeSrc = this.sanitizer.bypassSecurityTrustUrl(src.replace(/\"/g, ""));
        this.isShowQrCode = true;
        this.userService.onScanBindUserQrCode$
          .pipe(first())
          .subscribe(stomp => {
            this.user!.weChatUser = {openid: stomp.body};
            this.isShowQrCode = false;
          });
      });
  }

  onLogout(): void {
    /**
     * complete 时跳转
     */
    this.userService.logout()
      .subscribe(() => {
        }, (error) => {
          console.error('error', error);
        },
        () => {
          this.router.navigateByUrl('login').then();
        }
      );
  }

  onClose() {
    this.isShowQrCode = false;
  }

}
