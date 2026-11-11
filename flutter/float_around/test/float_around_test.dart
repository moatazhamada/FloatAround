import 'dart:ui';

import 'package:float_around/float_around.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  const pill = Rect.fromLTRB(12, 400, 92, 700);
  Rect row(double top, {double left = 0}) => Rect.fromLTRB(left, top, 1000, top + 50);
  double inset(Rect r, {Rect obstacle = pill, double feather = 140, bool rtl = false}) =>
      startInset(r, obstacle, gap: 12, feather: feather, rtl: rtl);

  test('row beside the pill gets the full intrusion plus gap', () {
    expect(inset(row(500)), closeTo(104, 0.01));
  });

  test('row far above the pill gets no inset', () {
    expect(inset(row(100)), 0);
  });

  test('row at the feather edge gets no inset', () {
    expect(inset(row(210)), 0);
  });

  test('row halfway through the feather gets half the inset', () {
    expect(inset(row(280)), closeTo(52, 0.01));
  });

  test('row already right of the pill gets no inset', () {
    expect(inset(row(500, left: 200)), 0);
  });

  test('rtl pill on the right edge insets from the right', () {
    expect(inset(row(500), obstacle: const Rect.fromLTRB(908, 400, 988, 700), rtl: true), closeTo(104, 0.01));
  });

  test('zero feather insets only overlapping rows', () {
    expect(inset(row(500), feather: 0), closeTo(104, 0.01));
    expect(inset(row(330), feather: 0), 0);
  });
}
