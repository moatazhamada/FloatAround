import 'package:float_around/float_around.dart';
import 'package:flutter/material.dart';

void main() => runApp(const FloatAroundDemo());

class FloatAroundDemo extends StatelessWidget {
  const FloatAroundDemo({super.key});

  @override
  Widget build(BuildContext context) => MaterialApp(
        title: 'FloatAround',
        theme: ThemeData(colorSchemeSeed: Colors.indigo),
        darkTheme: ThemeData(colorSchemeSeed: Colors.indigo, brightness: Brightness.dark),
        home: const DemoHome(),
      );
}

const _destinations = [
  (Icons.home, 'Home'),
  (Icons.search, 'Search'),
  (Icons.favorite, 'Saved'),
  (Icons.person, 'Profile'),
  (Icons.settings, 'Settings'),
];

const _paragraphs = [
  'Floating navigation is back. Tablets and unfolded foldables give us room to lift the rail off the edge and let content run underneath it, the way a modern sidebar floats over the page.',
  'But content that runs underneath also gets covered. A title, a row of cards or the first words of a paragraph end up hidden behind the pill.',
  'Padding the whole page wastes the space we just gained. The pill is short; most of the screen never touches it.',
  'So every row asks one question while it scrolls: how close am I to the pill? Rows beside it step aside. Rows near it step aside a little. Rows far away keep the full width.',
  'The easing is a smootherstep curve over a small feather distance, so a row starts moving before it reaches the pill and settles back after it leaves. No animation clock, the movement is tied to the scroll position itself.',
];

class DemoHome extends StatefulWidget {
  const DemoHome({super.key});

  @override
  State<DemoHome> createState() => _DemoHomeState();
}

class _DemoHomeState extends State<DemoHome> {
  final _obstacle = FloatingObstacle();
  var _selected = 0;

  @override
  void dispose() {
    _obstacle.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final wide = MediaQuery.sizeOf(context).width >= 600;
    final colors = Theme.of(context).colorScheme;
    return Scaffold(
      body: Stack(
        children: [
          _Feed(obstacle: _obstacle),
          if (wide)
            Align(
              alignment: AlignmentDirectional.centerStart,
              child: Padding(
                padding: const EdgeInsetsDirectional.only(start: 12),
                child: FloatingObstacleRegion(
                  obstacle: _obstacle,
                  child: _Pill(selected: _selected, onSelect: (i) => setState(() => _selected = i)),
                ),
              ),
            ),
          Positioned(
            top: 0,
            left: 0,
            right: 0,
            height: MediaQuery.paddingOf(context).top,
            child: ColoredBox(color: colors.surface.withValues(alpha: 0.92)),
          ),
        ],
      ),
      bottomNavigationBar: wide
          ? null
          : NavigationBar(
              selectedIndex: _selected,
              onDestinationSelected: (i) => setState(() => _selected = i),
              destinations: [for (final (icon, label) in _destinations) NavigationDestination(icon: Icon(icon), label: label)],
            ),
    );
  }
}

class _Pill extends StatelessWidget {
  const _Pill({required this.selected, required this.onSelect});

  final int selected;
  final ValueChanged<int> onSelect;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return Material(
      color: colors.surfaceContainer,
      elevation: 6,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(32),
        side: BorderSide(color: colors.outlineVariant),
      ),
      child: Padding(
        padding: const EdgeInsets.all(8),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          spacing: 4,
          children: [
            for (final (index, (icon, label)) in _destinations.indexed)
              IconButton(
                isSelected: index == selected,
                onPressed: () => onSelect(index),
                icon: Icon(icon),
                tooltip: label,
                style: IconButton.styleFrom(
                  fixedSize: const Size(56, 56),
                  backgroundColor: index == selected ? colors.secondaryContainer : null,
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class _Feed extends StatelessWidget {
  const _Feed({required this.obstacle});

  final FloatingObstacle obstacle;

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    return ListView(
      padding: const EdgeInsets.only(bottom: 32),
      children: [
        _Hero(obstacle: obstacle),
        for (var round = 1; round <= 3; round++) ...[
          _SectionHeader(title: 'Featured $round', obstacle: obstacle),
          _Carousel(obstacle: obstacle),
          _SectionHeader(title: 'The idea', obstacle: obstacle),
          for (final paragraph in _paragraphs)
            Avoid(
              obstacle: obstacle,
              child: Padding(
                padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
                child: Text(paragraph, style: text.bodyLarge),
              ),
            ),
          _SectionHeader(title: 'List rows', obstacle: obstacle),
          for (var i = 1; i <= 4; i++)
            Avoid(
              obstacle: obstacle,
              child: Card(
                margin: const EdgeInsets.fromLTRB(16, 0, 16, 12),
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Text('Row $i keeps its right edge and gives up only its start'),
                ),
              ),
            ),
        ],
      ],
    );
  }
}

class _Hero extends StatelessWidget {
  const _Hero({required this.obstacle});

  final FloatingObstacle obstacle;

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    return Container(
      height: 280,
      decoration: const BoxDecoration(
        gradient: LinearGradient(colors: [Color(0xFF3949AB), Color(0xFF00897B)]),
      ),
      child: Stack(
        children: [
          Align(
            alignment: AlignmentDirectional.bottomStart,
            child: Avoid(
              obstacle: obstacle,
              child: Padding(
                padding: const EdgeInsets.all(24),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('FloatAround', style: text.displaySmall?.copyWith(color: Colors.white, fontWeight: FontWeight.bold)),
                    const Text('The hero stays full-bleed. Only its text steps aside.', style: TextStyle(color: Colors.white)),
                  ],
                ),
              ),
            ),
          ),
          Align(
            alignment: AlignmentDirectional.topEnd,
            child: SafeArea(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: ListenableBuilder(
                  listenable: obstacle,
                  builder: (context, _) => FilterChip(
                    selected: obstacle.enabled,
                    onSelected: (value) => obstacle.enabled = value,
                    label: Text(obstacle.enabled ? 'Avoidance on' : 'Avoidance off'),
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _SectionHeader extends StatelessWidget {
  const _SectionHeader({required this.title, required this.obstacle});

  final String title;
  final FloatingObstacle obstacle;

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 12),
        child: Row(
          spacing: 12,
          children: [
            Expanded(child: Avoid(obstacle: obstacle, child: const Divider(thickness: 2))),
            Text(title, style: Theme.of(context).textTheme.titleLarge),
            const Expanded(child: Divider(thickness: 2)),
          ],
        ),
      );
}

class _Carousel extends StatelessWidget {
  const _Carousel({required this.obstacle});

  final FloatingObstacle obstacle;

  @override
  Widget build(BuildContext context) => AvoidanceBuilder(
        obstacle: obstacle,
        builder: (context, inset) => SizedBox(
          height: 132,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            padding: EdgeInsetsDirectional.only(start: 16 + inset, end: 16, top: 4, bottom: 8),
            itemCount: 10,
            separatorBuilder: (_, _) => const SizedBox(width: 12),
            itemBuilder: (context, index) => SizedBox(
              width: 160,
              child: Card(
                margin: EdgeInsets.zero,
                child: Align(
                  alignment: AlignmentDirectional.bottomStart,
                  child: Padding(
                    padding: const EdgeInsets.all(12),
                    child: Text('Card ${index + 1}', style: Theme.of(context).textTheme.titleMedium),
                  ),
                ),
              ),
            ),
          ),
        ),
      );
}
