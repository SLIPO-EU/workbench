export const defaultValues = {
  inputFormat: 'NT',
  outputFormat: 'NT',
  locale: 'en',
  similarity: 'jarowinkler',
  left: {
    id: 'a',
    file: null,
    categories: null,
  },
  right: {
    id: 'b',
    file: null,
    categories: null,
  },
  links: {
    id: 'links',
    file: null,
    linksFormat: null,
  },
  target: {
    id: 'target',
    mode: 'aa_mode',
    outputDir: null,
    fused: 'fused.nt',
    remaining: 'remaining.nt',
    ambiguous: 'review.nt',
    statistics: 'stats.json'
  },
  rulesSpec: null,
  profile: null,
};
