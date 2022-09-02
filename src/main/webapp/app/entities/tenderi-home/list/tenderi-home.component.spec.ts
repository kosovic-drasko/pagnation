import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';

import { TenderiHomeService } from '../service/tenderi-home.service';

import { TenderiHomeComponent } from './tenderi-home.component';

describe('TenderiHome Management Component', () => {
  let comp: TenderiHomeComponent;
  let fixture: ComponentFixture<TenderiHomeComponent>;
  let service: TenderiHomeService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      declarations: [TenderiHomeComponent],
    })
      .overrideTemplate(TenderiHomeComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(TenderiHomeComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(TenderiHomeService);

    const headers = new HttpHeaders();
    jest.spyOn(service, 'query').mockReturnValue(
      of(
        new HttpResponse({
          body: [{ id: 123 }],
          headers,
        })
      )
    );
  });

  it('Should call load all on init', () => {
    // WHEN
    comp.ngOnInit();

    // THEN
    expect(service.query).toHaveBeenCalled();
    expect(comp.tenderiHomes?.[0]).toEqual(expect.objectContaining({ id: 123 }));
  });
});
