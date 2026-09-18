enum TaskStatus {
  todo('TODO', 'A faire'),
  inProgress('IN_PROGRESS', 'En cours'),
  done('DONE', 'Terminee');

  const TaskStatus(this.apiValue, this.label);
  final String apiValue;
  final String label;

  static TaskStatus fromApi(String value) =>
      values.firstWhere((status) => status.apiValue == value);
}

enum TaskPriority {
  low('LOW', 'Basse'),
  medium('MEDIUM', 'Moyenne'),
  high('HIGH', 'Haute');

  const TaskPriority(this.apiValue, this.label);
  final String apiValue;
  final String label;

  static TaskPriority fromApi(String? value) => values.firstWhere(
    (priority) => priority.apiValue == value,
    orElse: () => TaskPriority.medium,
  );
}

class Task {
  const Task({
    required this.id,
    required this.title,
    required this.description,
    required this.status,
    required this.priority,
    this.dueDate,
  });

  final String id;
  final String title;
  final String description;
  final TaskStatus status;
  final TaskPriority priority;
  final DateTime? dueDate;

  factory Task.fromJson(Map<String, dynamic> json) => Task(
    id: json['id'] as String,
    title: json['title'] as String,
    description: json['description'] as String? ?? '',
    status: TaskStatus.fromApi(json['status'] as String),
    priority: TaskPriority.fromApi(json['priority'] as String?),
    dueDate: json['dueDate'] == null
        ? null
        : DateTime.parse(json['dueDate'] as String),
  );

  Map<String, dynamic> toRequest({TaskStatus? statusOverride}) => {
    'title': title,
    'description': description,
    'status': (statusOverride ?? status).apiValue,
    'priority': priority.apiValue,
    'dueDate': dueDate?.toIso8601String().split('T').first,
  };
}
