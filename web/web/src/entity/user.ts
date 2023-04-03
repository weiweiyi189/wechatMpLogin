import {WeChatUser} from './we-chat-user';

export interface User {
  /** id */
  id: number | undefined;

  username: string | undefined;

  /**
   * 密码
   */
  password: string | undefined;

  /**
   * 姓名
   */
  name: string | undefined;


  weChatUser: WeChatUser;

}
