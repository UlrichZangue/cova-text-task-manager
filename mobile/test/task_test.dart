import 'package:flutter_test/flutter_test.dart';
import 'package:task_manager_mobile/features/tasks/domain/task.dart';

void main() {
  test('parse une tache retournee par l API', () {
    final task = Task.fromJson({
      'id': '550e8400-e29b-41d4-a716-446655440000',
      'title': 'Preparer le rapport',
      'description': 'Verifier les chiffres',
      'status': 'IN_PROGRESS',
      'priority': 'HIGH',
      'dueDate': '2026-10-01',
    });

    expect(task.status, TaskStatus.inProgress);
    expect(task.priority, TaskPriority.high);
    expect(task.dueDate, DateTime(2026, 10));
  });

  test('produit le DTO attendu pour une mise a jour', () {
    const task = Task(
      id: 'task-id',
      title: 'Tache',
      description: '',
      status: TaskStatus.todo,
      priority: TaskPriority.low,
    );

    expect(task.toRequest(statusOverride: TaskStatus.done), {
      'title': 'Tache',
      'description': '',
      'status': 'DONE',
      'priority': 'LOW',
      'dueDate': null,
    });
  });
}
