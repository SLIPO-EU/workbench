import {
  EnumDataFormat,
} from "../enum";

export const defaultValues = {
  prefixes: [
    {
      namespace: 'http://slipo.eu/def#',
      label: 'slipo'
    },
    {
      namespace: 'http://www.w3.org/2002/07/owl#',
      label: 'owl'
    }
  ],
  source: {
    id: 'a',
    endpoint: 'a.nt',
    var: '?x',
    pageSize: -1,
    restrictions: [
      ''
    ],
    properties: [
      'slipo:name/slipo:nameValue RENAME label'
    ],
    dataFormat: EnumDataFormat.N_TRIPLES,
  },
  target: {
    id: 'b',
    endpoint: 'b.nt',
    var: '?y',
    pageSize: -1,
    restrictions: [
      ''
    ],
    properties: [
      'slipo:name/slipo:nameValue RENAME label'
    ],
    dataFormat: EnumDataFormat.N_TRIPLES,
  },
  metric: 'trigrams(x.label, y.label)',
  acceptance: {
    threshold: 0.96,
    file: 'accepted.nt',
    relation: 'owl:sameAs'
  },
  review: {
    threshold: 0.90,
    file: 'review.nt',
    relation: 'owl:sameAs'
  },
  execution: {
    rewriter: 'default',
    planner: 'default',
    engine: 'default'
  },
  outputFormat: EnumDataFormat.N_TRIPLES,
  profile: null,
};
