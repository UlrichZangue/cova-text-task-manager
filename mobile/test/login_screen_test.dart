import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:task_manager_mobile/core/network/api_client.dart';
import 'package:task_manager_mobile/core/storage/token_storage.dart';
import 'package:task_manager_mobile/features/auth/data/auth_repository.dart';
import 'package:task_manager_mobile/features/auth/presentation/auth_controller.dart';
import 'package:task_manager_mobile/features/auth/presentation/login_screen.dart';

class _MemoryTokenStorage implements TokenStorage {
  String? token;

  @override
  Future<void> clear() async => token = null;

  @override
  Future<String?> read() async => token;

  @override
  Future<void> write(String token) async => this.token = token;
}

void main() {
  testWidgets('affiche et valide le formulaire de connexion', (tester) async {
    final storage = _MemoryTokenStorage();
    final controller = AuthController(
      repository: AuthRepository(ApiClient(tokenStorage: storage, dio: Dio())),
      tokenStorage: storage,
    );

    await tester.pumpWidget(
      MaterialApp(home: LoginScreen(controller: controller)),
    );

    expect(find.text('Task Manager'), findsOneWidget);
    expect(find.text('Se connecter'), findsOneWidget);
    await tester.tap(find.text('Se connecter'));
    await tester.pump();
    expect(find.text('Saisissez une adresse email valide.'), findsOneWidget);
    expect(find.text('Le mot de passe est obligatoire.'), findsOneWidget);
  });
}
