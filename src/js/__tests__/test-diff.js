
jest.unmock('../util/diff-shallow');
jest.unmock('lodash');

const computeDiff = require('../util/diff-shallow');

describe('Test util/diff-shallow', function () {
  it('Test A', function () {
    var o1 = {aa: 99, bb: 100, cc: 199};
    var o2 = {bb: 100, cc: 299, dd: 170};
    var computed = computeDiff(o1, o2);
    var expected = {aa: 99, cc: 199};
    expect(computed).toEqual(expected);
  });
});
