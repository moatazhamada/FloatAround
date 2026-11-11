## 0.2.0

* The pill can float on either edge: the side is taken from where the obstacle sits, so a right-hand pill works in left-to-right layouts (iPhone Duo camera column) as well as right-to-left.
* Breaking: `startInset` is now `avoidanceInsets`, returning `EdgeInsets`; `AvoidanceBuilder` hands its builder `EdgeInsets` instead of a `double`; `FloatingObstacle.insetFor` is now `insetsFor`.
* Example: on iOS the pill sits in the camera column below the status area, and rows reclaim that column below it; on iPad and Android it floats on the leading edge.

## 0.1.0

* Initial release: `startInset`, `FloatingObstacle`, `FloatingObstacleRegion`, `Avoid`, `AvoidanceBuilder`.
