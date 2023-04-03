import {Component, Input} from '@angular/core';

/**
 * 弹出框
 */
@Component({
  selector: 'app-yz-modal',
  templateUrl: './yz-modal.component.html',
  styleUrls: ['./yz-modal.component.scss']
})
export class YzModalComponent {
  @Input()
  size = 'md';

  @Input()
  width = '30%';

  constructor() {
  }

}
