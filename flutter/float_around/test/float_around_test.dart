import 'package:float_around/float_around.dart';
import 'package:flutter/painting.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  const leftPill = Rect.fromLTRB(12, 400, 92, 700);
  const rightPill = Rect.fromLTRB(908, 400, 988, 700);
  Rect row(double top, {double left = 0}) => Rect.fromLTRB(left, top, 1000, top + 50);
  EdgeInsets insets(Rect r, {Rect obstacle = leftPill, double feather = 140}) =>
      avoidanceInsets(r, obstacle, gap: 12, feather: feather);

  test('row beside a left pill gets the full intrusion plus gap on the left', () {
    expect(insets(row(500)).left, closeTo(104, 0.01));
    expect(insets(row(500)).right, 0);
  });

  test('row beside a right pill gets the full intrusion plus gap on the right', () {
    expect(insets(row(500), obstacle: rightPill).right, closeTo(104, 0.01));
    expect(insets(row(500), obstacle: rightPill).left, 0);
  });

  test('row far above the pill gets no inset', () {
    expect(insets(row(100)), EdgeInsets.zero);
  });

  test('row at the feather edge gets no inset', () {
    expect(insets(row(210)), EdgeInsets.zero);
  });

  test('row halfway through the feather gets half the inset', () {
    expect(insets(row(280)).left, closeTo(52, 0.01));
  });

  test('row already clear of the pill gets no inset', () {
    expect(insets(row(500, left: 200)), EdgeInsets.zero);
  });

  test('zero feather insets only overlapping rows', () {
    expect(insets(row(500), feather: 0).left, closeTo(104, 0.01));
    expect(insets(row(330), feather: 0), EdgeInsets.zero);
  });
}
