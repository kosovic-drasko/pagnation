export interface IPonude {
  id?: number;
  broj?: number | null;
  ime?: string | null;
}

export class Ponude implements IPonude {
  constructor(public id?: number, public broj?: number | null, public ime?: string | null) {}
}

export function getPonudeIdentifier(ponude: IPonude): number | undefined {
  return ponude.id;
}
