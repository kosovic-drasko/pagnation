import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';

import { ITenderiHome } from '../tenderi-home.model';
import { TenderiHomeService } from '../service/tenderi-home.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'jhi-tenderi-home',
  templateUrl: './tenderi-home.component.html',
})
export class TenderiHomeComponent implements OnInit {
  tenderiHomes?: ITenderiHome[];
  isLoading = false;
  sifra?: any;
  constructor(protected activatedRoute: ActivatedRoute) {}
  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      this.sifra = params['sifra'];
    });
  }
}
