import 'package:flutter/material.dart';

import 'app.dart';
import 'core/network/api_client.dart';
import 'core/storage/token_storage.dart';
import 'features/auth/data/auth_repository.dart';
import 'features/auth/presentation/auth_controller.dart';
import 'features/tasks/data/task_repository.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  const tokenStorage = SecureTokenStorage();
  final apiClient = ApiClient(tokenStorage: tokenStorage);
  final authController = AuthController(
    repository: AuthRepository(apiClient),
    tokenStorage: tokenStorage,
  );
  await authController.restoreSession();
  runApp(
    TaskManagerApp(
      authController: authController,
      taskRepository: TaskRepository(apiClient),
    ),
  );
}
