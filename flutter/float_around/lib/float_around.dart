library;

import 'dart:math' as math;

import 'package:flutter/scheduler.dart';
import 'package:flutter/widgets.dart';

/// How far [row] must give up its start edge to clear [obstacle].
///
/// Rows that overlap the obstacle vertically get the full intrusion plus [gap]. Rows within
/// [feather] of it get a smootherstep-eased share. Rows further away, or already clear of it
/// horizontally, get zero. With [rtl] the obstacle is expected on the right edge.
double startInset(
  Rect row,
  Rect obstacle, {
  required double gap,
  required double feather,
  bool rtl = false,
}) {
  final intrusion = rtl ? row.right - obstacle.left + gap : obstacle.right + gap - row.left;
  if (intrusion <= 0) return 0;
  final distance = math.max(math.max(obstacle.top - row.bottom, row.top - obstacle.bottom), 0.0);
  if (distance == 0) return intrusion;
  if (distance >= feather) return 0;
  final t = 1 - distance / feather;
  return intrusion * t * t * t * (t * (t * 6 - 15) + 10);
}

/// A floating overlay (rail, pill, toolbar) that content should step around.
class FloatingObstacle extends ChangeNotifier {
  FloatingObstacle({this.gap = 12, this.feather = 140});

  /// Space kept between the overlay and content beside it.
  final double gap;

  /// Vertical distance over which content eases in and out of its avoiding position.
  final double feather;

  Rect? _bounds;

  /// Current bounds of the overlay in global coordinates, or null when it is not on screen.
  Rect? get bounds => _bounds;

  bool _enabled = true;

  /// When false every avoiding element keeps its full width.
  bool get enabled => _enabled;
  set enabled(bool value) {
    if (value == _enabled) return;
    _enabled = value;
    notifyListeners();
  }

  void _setBounds(Rect? value) {
    if (value == _bounds) return;
    _bounds = value;
    notifyListeners();
  }

  /// The start inset for an element at [row] in global coordinates.
  double insetFor(Rect row, TextDirection direction) {
    final obstacle = _bounds;
    if (!_enabled || obstacle == null) return 0;
    return startInset(row, obstacle, gap: gap, feather: feather, rtl: direction == TextDirection.rtl);
  }
}

/// Publishes [child]'s bounds as the [obstacle].
class FloatingObstacleRegion extends StatefulWidget {
  const FloatingObstacleRegion({super.key, required this.obstacle, required this.child});

  final FloatingObstacle obstacle;
  final Widget child;

  @override
  State<FloatingObstacleRegion> createState() => _FloatingObstacleRegionState();
}

class _FloatingObstacleRegionState extends State<FloatingObstacleRegion> {
  @override
  void dispose() {
    widget.obstacle._setBounds(null);
    super.dispose();
  }

  void _measure(Duration _) {
    if (!mounted) return;
    final box = context.findRenderObject();
    if (box is! RenderBox || !box.hasSize) return;
    widget.obstacle._setBounds(box.localToGlobal(Offset.zero) & box.size);
  }

  @override
  Widget build(BuildContext context) {
    MediaQuery.sizeOf(context);
    SchedulerBinding.instance.addPostFrameCallback(_measure);
    return widget.child;
  }
}

/// Rebuilds with the start inset this widget currently needs to clear [obstacle].
///
/// Use it when the inset should feed padding rather than shift the frame, for example a
/// horizontal list whose first item steps aside while the list itself stays full width.
class AvoidanceBuilder extends StatefulWidget {
  const AvoidanceBuilder({super.key, required this.obstacle, required this.builder});

  final FloatingObstacle obstacle;
  final Widget Function(BuildContext context, double inset) builder;

  @override
  State<AvoidanceBuilder> createState() => _AvoidanceBuilderState();
}

class _AvoidanceBuilderState extends State<AvoidanceBuilder> {
  double _inset = 0;
  ScrollPosition? _position;
  bool _measurePending = false;

  @override
  void initState() {
    super.initState();
    widget.obstacle.addListener(_scheduleMeasure);
  }

  @override
  void didUpdateWidget(AvoidanceBuilder oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.obstacle != widget.obstacle) {
      oldWidget.obstacle.removeListener(_scheduleMeasure);
      widget.obstacle.addListener(_scheduleMeasure);
      _scheduleMeasure();
    }
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    MediaQuery.sizeOf(context);
    final position = Scrollable.maybeOf(context)?.position;
    if (position != _position) {
      _position?.removeListener(_scheduleMeasure);
      _position = position?..addListener(_scheduleMeasure);
    }
    _scheduleMeasure();
  }

  @override
  void dispose() {
    widget.obstacle.removeListener(_scheduleMeasure);
    _position?.removeListener(_scheduleMeasure);
    super.dispose();
  }

  // ponytail: positions are read after the frame, so the inset trails the scroll by one frame.
  // Invisible at normal speeds; computing the position inside layout would remove it.
  void _scheduleMeasure() {
    if (_measurePending) return;
    _measurePending = true;
    SchedulerBinding.instance.addPostFrameCallback(_measure);
    SchedulerBinding.instance.ensureVisualUpdate();
  }

  void _measure(Duration _) {
    _measurePending = false;
    if (!mounted) return;
    final box = context.findRenderObject();
    if (box is! RenderBox || !box.hasSize) return;
    final rect = box.localToGlobal(Offset.zero) & box.size;
    final inset = widget.obstacle.insetFor(rect, Directionality.of(context));
    if ((inset - _inset).abs() >= 0.5) setState(() => _inset = inset);
  }

  @override
  Widget build(BuildContext context) => widget.builder(context, _inset);
}

/// Gives up as much of [child]'s start edge as needed to clear [obstacle], depending on how
/// close it currently is. The end edge stays where it is.
class Avoid extends StatelessWidget {
  const Avoid({super.key, required this.obstacle, required this.child});

  final FloatingObstacle obstacle;
  final Widget child;

  @override
  Widget build(BuildContext context) => AvoidanceBuilder(
        obstacle: obstacle,
        builder: (context, inset) => Padding(
          padding: EdgeInsetsDirectional.only(start: inset),
          child: child,
        ),
      );
}
