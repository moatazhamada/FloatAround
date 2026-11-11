# float_around

Content that steps around a floating overlay, row by row, as you scroll.

A floating navigation pill on a tablet or unfolded foldable covers whatever scrolls underneath it. Padding the whole page wastes the space the pill was meant to free. `float_around` gives every widget its own exclusion zone: widgets beside the pill step aside fully, widgets within a feather distance step aside partially (smootherstep), widgets further away keep the full width. The pill can sit on either edge; each widget pads the side the pill is on.

![demo](https://raw.githubusercontent.com/moatazhamada/FloatAround/main/docs/demo.gif)

## Usage

```dart
final obstacle = FloatingObstacle(gap: 12, feather: 140);

// The pill publishes its bounds
FloatingObstacleRegion(obstacle: obstacle, child: MyPill());

// Anything that should step aside
Avoid(obstacle: obstacle, child: Text(paragraph));

// Containers that should shift their padding instead of their frame
AvoidanceBuilder(
  obstacle: obstacle,
  builder: (context, insets) => ListView(
    scrollDirection: Axis.horizontal,
    padding: EdgeInsets.fromLTRB(16 + insets.left, 0, 16 + insets.right, 0),
    children: cards,
  ),
);
```

`obstacle.enabled = false` switches avoidance off.

`avoidanceInsets(row, obstacle, gap:, feather:)` is the pure function behind it, if you want to apply the padding yourself.

## iOS

On iPhone Duo, iOS keeps a column on the camera side for the camera and status, and puts vertical bars in it right below them. The example does the same: the obstacle covers the status block plus the pill, and rows reclaim the column everywhere below the pill instead of leaving it empty. On iPad the pill floats on the leading edge, like the system sidebar.

The same idea for Jetpack Compose, plus an idea paper, lives at https://github.com/moatazhamada/FloatAround.
