import 'dart:async';

import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../data/task_repository.dart';
import '../domain/task.dart';
import 'task_form_sheet.dart';

class TaskListScreen extends StatefulWidget {
  const TaskListScreen({
    required this.repository,
    required this.onLogout,
    super.key,
  });

  final TaskRepository repository;
  final Future<void> Function() onLogout;

  @override
  State<TaskListScreen> createState() => _TaskListScreenState();
}

class _TaskListScreenState extends State<TaskListScreen> {
  final _searchController = TextEditingController();
  Timer? _debounce;
  List<Task> _tasks = const [];
  TaskStatus? _status;
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadTasks();
  }

  @override
  void dispose() {
    _debounce?.cancel();
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _loadTasks() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final tasks = await widget.repository.findAll(
        search: _searchController.text,
        status: _status,
      );
      if (mounted) setState(() => _tasks = tasks);
    } on ApiException catch (error) {
      if (error.statusCode == 401) {
        await widget.onLogout();
        return;
      }
      if (mounted) setState(() => _error = error.message);
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  void _onSearchChanged(String _) {
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 400), _loadTasks);
  }

  Future<void> _openForm([Task? existing]) async {
    final task = await showModalBottomSheet<Task>(
      context: context,
      isScrollControlled: true,
      useSafeArea: true,
      builder: (_) => TaskFormSheet(task: existing),
    );
    if (task == null) return;
    try {
      if (existing == null) {
        await widget.repository.create(task);
      } else {
        await widget.repository.update(task);
      }
      if (!mounted) return;
      _showMessage(existing == null ? 'Tache creee.' : 'Tache modifiee.');
      await _loadTasks();
    } on ApiException catch (error) {
      if (mounted) _showMessage(error.message, error: true);
    }
  }

  Future<void> _changeStatus(Task task, TaskStatus status) async {
    try {
      await widget.repository.update(task, status: status);
      if (!mounted) return;
      _showMessage('Statut mis a jour.');
      await _loadTasks();
    } on ApiException catch (error) {
      if (mounted) _showMessage(error.message, error: true);
    }
  }

  Future<void> _delete(Task task) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Supprimer la tache ?'),
        content: Text('"${task.title}" sera supprimee definitivement.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Annuler'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(context).pop(true),
            child: const Text('Supprimer'),
          ),
        ],
      ),
    );
    if (confirmed != true) return;
    try {
      await widget.repository.delete(task.id);
      if (!mounted) return;
      _showMessage('Tache supprimee.');
      await _loadTasks();
    } on ApiException catch (error) {
      if (mounted) _showMessage(error.message, error: true);
    }
  }

  void _showMessage(String message, {bool error = false}) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: error ? Theme.of(context).colorScheme.error : null,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Mes taches'),
        actions: [
          IconButton(
            tooltip: 'Actualiser',
            onPressed: _loading ? null : _loadTasks,
            icon: const Icon(Icons.refresh),
          ),
          IconButton(
            tooltip: 'Se deconnecter',
            onPressed: widget.onLogout,
            icon: const Icon(Icons.logout),
          ),
        ],
      ),
      body: SafeArea(
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 12),
              child: TextField(
                controller: _searchController,
                onChanged: _onSearchChanged,
                decoration: InputDecoration(
                  hintText: 'Rechercher une tache',
                  prefixIcon: const Icon(Icons.search),
                  suffixIcon: _searchController.text.isEmpty
                      ? null
                      : IconButton(
                          tooltip: 'Effacer la recherche',
                          onPressed: () {
                            _searchController.clear();
                            _loadTasks();
                          },
                          icon: const Icon(Icons.close),
                        ),
                ),
              ),
            ),
            SizedBox(
              height: 48,
              child: ListView(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                scrollDirection: Axis.horizontal,
                children: [
                  _FilterChip(
                    label: 'Toutes',
                    selected: _status == null,
                    onSelected: () => _setStatus(null),
                  ),
                  for (final status in TaskStatus.values)
                    _FilterChip(
                      label: status.label,
                      selected: _status == status,
                      onSelected: () => _setStatus(status),
                    ),
                ],
              ),
            ),
            Expanded(child: _buildContent()),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _openForm(),
        icon: const Icon(Icons.add),
        label: const Text('Nouvelle tache'),
      ),
    );
  }

  void _setStatus(TaskStatus? status) {
    setState(() => _status = status);
    _loadTasks();
  }

  Widget _buildContent() {
    if (_loading) return const Center(child: CircularProgressIndicator());
    if (_error != null) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.cloud_off_outlined, size: 48),
              const SizedBox(height: 12),
              Text(_error!, textAlign: TextAlign.center),
              const SizedBox(height: 16),
              OutlinedButton.icon(
                onPressed: _loadTasks,
                icon: const Icon(Icons.refresh),
                label: const Text('Reessayer'),
              ),
            ],
          ),
        ),
      );
    }
    if (_tasks.isEmpty) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.inbox_outlined, size: 52),
              const SizedBox(height: 12),
              Text(
                'Aucune tache trouvee.',
                style: Theme.of(context).textTheme.titleMedium,
              ),
              const SizedBox(height: 16),
              FilledButton.icon(
                onPressed: () => _openForm(),
                icon: const Icon(Icons.add),
                label: const Text('Creer une tache'),
              ),
            ],
          ),
        ),
      );
    }
    return RefreshIndicator(
      onRefresh: _loadTasks,
      child: ListView.separated(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 96),
        itemCount: _tasks.length,
        separatorBuilder: (_, _) => const SizedBox(height: 10),
        itemBuilder: (context, index) {
          final task = _tasks[index];
          return _TaskCard(
            task: task,
            onEdit: () => _openForm(task),
            onDelete: () => _delete(task),
            onStatusChanged: (status) => _changeStatus(task, status),
          );
        },
      ),
    );
  }
}

class _FilterChip extends StatelessWidget {
  const _FilterChip({
    required this.label,
    required this.selected,
    required this.onSelected,
  });
  final String label;
  final bool selected;
  final VoidCallback onSelected;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(right: 8),
      child: ChoiceChip(
        label: Text(label),
        selected: selected,
        onSelected: (_) => onSelected(),
      ),
    );
  }
}

class _TaskCard extends StatelessWidget {
  const _TaskCard({
    required this.task,
    required this.onEdit,
    required this.onDelete,
    required this.onStatusChanged,
  });

  final Task task;
  final VoidCallback onEdit;
  final VoidCallback onDelete;
  final ValueChanged<TaskStatus> onStatusChanged;

  @override
  Widget build(BuildContext context) {
    final color = switch (task.priority) {
      TaskPriority.high => Colors.red,
      TaskPriority.medium => Colors.amber.shade800,
      TaskPriority.low => Colors.green,
    };
    return Card(
      child: InkWell(
        onTap: onEdit,
        borderRadius: BorderRadius.circular(8),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(
                    child: Text(
                      task.title,
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ),
                  PopupMenuButton<String>(
                    tooltip: 'Actions',
                    onSelected: (value) {
                      if (value == 'edit') onEdit();
                      if (value == 'delete') onDelete();
                    },
                    itemBuilder: (_) => const [
                      PopupMenuItem(value: 'edit', child: Text('Modifier')),
                      PopupMenuItem(value: 'delete', child: Text('Supprimer')),
                    ],
                  ),
                ],
              ),
              if (task.description.isNotEmpty) ...[
                const SizedBox(height: 6),
                Text(
                  task.description,
                  maxLines: 3,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
              const SizedBox(height: 14),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                crossAxisAlignment: WrapCrossAlignment.center,
                children: [
                  PopupMenuButton<TaskStatus>(
                    tooltip: 'Changer le statut',
                    onSelected: onStatusChanged,
                    itemBuilder: (_) => TaskStatus.values
                        .map(
                          (status) => PopupMenuItem(
                            value: status,
                            child: Text(status.label),
                          ),
                        )
                        .toList(),
                    child: Chip(
                      avatar: const Icon(Icons.sync_alt, size: 18),
                      label: Text(task.status.label),
                    ),
                  ),
                  Chip(
                    avatar: Icon(Icons.flag_outlined, color: color, size: 18),
                    label: Text(task.priority.label),
                  ),
                  if (task.dueDate != null)
                    Chip(
                      avatar: const Icon(Icons.event_outlined, size: 18),
                      label: Text(_formatDate(task.dueDate!)),
                    ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  String _formatDate(DateTime date) =>
      '${date.day.toString().padLeft(2, '0')}/${date.month.toString().padLeft(2, '0')}/${date.year}';
}
