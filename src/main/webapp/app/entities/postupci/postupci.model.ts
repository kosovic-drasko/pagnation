export interface IPostupci {
  id?: number;
  broj?: number | null;
  ime?: string | null;
}

export class Postupci implements IPostupci {
  constructor(public id?: number, public broj?: number | null, public ime?: string | null) {}
}

export function getPostupciIdentifier(postupci: IPostupci): number | undefined {
  return postupci.id;
}
