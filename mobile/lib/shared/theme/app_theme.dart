import 'package:flutter/material.dart';

abstract final class AppTheme {
  static const _seed = Color(0xFF176B87);

  static ThemeData get light => _theme(Brightness.light);
  static ThemeData get dark => _theme(Brightness.dark);

  static ThemeData _theme(Brightness brightness) {
    final colors = ColorScheme.fromSeed(
      seedColor: _seed,
      brightness: brightness,
    );
    return ThemeData(
      colorScheme: colors,
      useMaterial3: true,
      scaffoldBackgroundColor: brightness == Brightness.light
          ? const Color(0xFFF7F8FA)
          : const Color(0xFF101416),
      inputDecorationTheme: const InputDecorationTheme(
        border: OutlineInputBorder(),
      ),
      cardTheme: const CardThemeData(margin: EdgeInsets.zero, elevation: 0),
    );
  }
}
