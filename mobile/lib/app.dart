import 'package:flutter/material.dart';

import 'features/auth/presentation/auth_controller.dart';
import 'features/auth/presentation/login_screen.dart';
import 'features/tasks/data/task_repository.dart';
import 'features/tasks/presentation/task_list_screen.dart';
import 'shared/theme/app_theme.dart';

class TaskManagerApp extends StatelessWidget {
  const TaskManagerApp({
    required this.authController,
    required this.taskRepository,
    super.key,
  });

  final AuthController authController;
  final TaskRepository taskRepository;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Task Manager',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light,
      darkTheme: AppTheme.dark,
      themeMode: ThemeMode.system,
      home: ListenableBuilder(
        listenable: authController,
        builder: (context, _) {
          if (authController.isAuthenticated) {
            return TaskListScreen(
              repository: taskRepository,
              onLogout: authController.logout,
            );
          }
          return LoginScreen(controller: authController);
        },
      ),
    );
  }
}
