import 'package:flutter/material.dart';

import '../domain/task.dart';

class TaskFormSheet extends StatefulWidget {
  const TaskFormSheet({this.task, super.key});

  final Task? task;

  @override
  State<TaskFormSheet> createState() => _TaskFormSheetState();
}

class _TaskFormSheetState extends State<TaskFormSheet> {
  final _formKey = GlobalKey<FormState>();
  late final TextEditingController _title;
  late final TextEditingController _description;
  late TaskStatus _status;
  late TaskPriority _priority;
  DateTime? _dueDate;

  @override
  void initState() {
    super.initState();
    _title = TextEditingController(text: widget.task?.title);
    _description = TextEditingController(text: widget.task?.description);
    _status = widget.task?.status ?? TaskStatus.todo;
    _priority = widget.task?.priority ?? TaskPriority.medium;
    _dueDate = widget.task?.dueDate;
  }

  @override
  void dispose() {
    _title.dispose();
    _description.dispose();
    super.dispose();
  }

  void _submit() {
    if (!_formKey.currentState!.validate()) return;
    Navigator.of(context).pop(
      Task(
        id: widget.task?.id ?? '',
        title: _title.text.trim(),
        description: _description.text.trim(),
        status: _status,
        priority: _priority,
        dueDate: _dueDate,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final bottomInset = MediaQuery.viewInsetsOf(context).bottom;
    return SafeArea(
      child: Padding(
        padding: EdgeInsets.fromLTRB(20, 12, 20, bottomInset + 20),
        child: SingleChildScrollView(
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              mainAxisSize: MainAxisSize.min,
              children: [
                Row(
                  children: [
                    Expanded(
                      child: Text(
                        widget.task == null
                            ? 'Nouvelle tache'
                            : 'Modifier la tache',
                        style: Theme.of(context).textTheme.titleLarge,
                      ),
                    ),
                    IconButton(
                      tooltip: 'Fermer',
                      onPressed: () => Navigator.of(context).pop(),
                      icon: const Icon(Icons.close),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _title,
                  autofocus: true,
                  maxLength: 150,
                  decoration: const InputDecoration(labelText: 'Titre'),
                  validator: (value) => value == null || value.trim().isEmpty
                      ? 'Le titre est obligatoire.'
                      : null,
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _description,
                  minLines: 3,
                  maxLines: 5,
                  decoration: const InputDecoration(labelText: 'Description'),
                ),
                const SizedBox(height: 16),
                DropdownButtonFormField<TaskStatus>(
                  initialValue: _status,
                  decoration: const InputDecoration(labelText: 'Statut'),
                  items: TaskStatus.values
                      .map(
                        (status) => DropdownMenuItem(
                          value: status,
                          child: Text(status.label),
                        ),
                      )
                      .toList(),
                  onChanged: (value) => _status = value!,
                ),
                const SizedBox(height: 16),
                DropdownButtonFormField<TaskPriority>(
                  initialValue: _priority,
                  decoration: const InputDecoration(labelText: 'Priorite'),
                  items: TaskPriority.values
                      .map(
                        (priority) => DropdownMenuItem(
                          value: priority,
                          child: Text(priority.label),
                        ),
                      )
                      .toList(),
                  onChanged: (value) => _priority = value!,
                ),
                const SizedBox(height: 16),
                OutlinedButton.icon(
                  onPressed: () async {
                    final date = await showDatePicker(
                      context: context,
                      firstDate: DateTime.now().subtract(
                        const Duration(days: 365),
                      ),
                      lastDate: DateTime.now().add(const Duration(days: 3650)),
                      initialDate: _dueDate ?? DateTime.now(),
                    );
                    if (date != null) setState(() => _dueDate = date);
                  },
                  icon: const Icon(Icons.event_outlined),
                  label: Text(
                    _dueDate == null
                        ? 'Ajouter une echeance'
                        : 'Echeance : ${_formatDate(_dueDate!)}',
                  ),
                ),
                const SizedBox(height: 20),
                FilledButton.icon(
                  onPressed: _submit,
                  icon: const Icon(Icons.save_outlined),
                  label: Text(widget.task == null ? 'Creer' : 'Enregistrer'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  String _formatDate(DateTime date) =>
      '${date.day.toString().padLeft(2, '0')}/${date.month.toString().padLeft(2, '0')}/${date.year}';
}
