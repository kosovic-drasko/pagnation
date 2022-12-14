import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'ponude',
        data: { pageTitle: 'Ponudes' },
        loadChildren: () => import('./ponude/ponude.module').then(m => m.PonudeModule),
      },
      {
        path: 'postupci',
        data: { pageTitle: 'Postupcis' },
        loadChildren: () => import('./postupci/postupci.module').then(m => m.PostupciModule),
      },
      {
        path: 'tenderi-home',
        data: { pageTitle: 'TenderiHomes' },
        loadChildren: () => import('./tenderi-home/tenderi-home.module').then(m => m.TenderiHomeModule),
      },
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ]),
  ],
})
export class EntityRoutingModule {}
