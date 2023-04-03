/**
 * websocket的返回数据
 */
export interface WebSocketData {
  body: string;
  command: "MESSAGE";
  headers: {[key: string]: string};
}
