import 'package:flutter/foundation.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/storage/token_storage.dart';
import '../data/auth_repository.dart';

class AuthController extends ChangeNotifier {
  AuthController({required this.repository, required this.tokenStorage});

  final AuthRepository repository;
  final TokenStorage tokenStorage;
  bool isAuthenticated = false;
  bool isLoading = false;
  String? error;

  Future<void> restoreSession() async {
    isAuthenticated = await tokenStorage.read() != null;
    notifyListeners();
  }

  Future<bool> login(String email, String password) async {
    return _run(() async {
      final token = await repository.login(email: email, password: password);
      await tokenStorage.write(token);
      isAuthenticated = true;
    });
  }

  Future<bool> register(String name, String email, String password) async {
    return _run(() async {
      await repository.register(name: name, email: email, password: password);
      final token = await repository.login(email: email, password: password);
      await tokenStorage.write(token);
      isAuthenticated = true;
    });
  }

  Future<void> logout() async {
    await tokenStorage.clear();
    isAuthenticated = false;
    notifyListeners();
  }

  Future<bool> _run(Future<void> Function() action) async {
    isLoading = true;
    error = null;
    notifyListeners();
    try {
      await action();
      return true;
    } on ApiException catch (exception) {
      error = exception.message;
      return false;
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }
}
