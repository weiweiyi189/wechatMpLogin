import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import { YzModalComponent } from './yz-modal.component';

describe('YzModalComponent', () => {
  let component: YzModalComponent;
  let fixture: ComponentFixture<YzModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ YzModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(YzModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
