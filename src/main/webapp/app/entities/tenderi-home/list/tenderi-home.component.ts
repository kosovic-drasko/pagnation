import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';

import { ITenderiHome } from '../tenderi-home.model';
import { TenderiHomeService } from '../service/tenderi-home.service';

@Component({
  selector: 'jhi-tenderi-home',
  templateUrl: './tenderi-home.component.html',
})
export class TenderiHomeComponent implements OnInit {
  tenderiHomes?: ITenderiHome[];
  isLoading = false;

  constructor(protected tenderiHomeService: TenderiHomeService) {}

  loadAll(): void {
    this.isLoading = true;

    this.tenderiHomeService.query().subscribe({
      next: (res: HttpResponse<ITenderiHome[]>) => {
        this.isLoading = false;
        this.tenderiHomes = res.body ?? [];
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  ngOnInit(): void {
    this.loadAll();
  }

  trackId(_index: number, item: ITenderiHome): number {
    return item.id!;
  }
}
