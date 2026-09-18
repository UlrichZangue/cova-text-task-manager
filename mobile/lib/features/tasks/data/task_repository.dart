import '../../../core/network/api_client.dart';
import '../domain/task.dart';

class TaskRepository {
  const TaskRepository(this._client);
  final ApiClient _client;

  Future<List<Task>> findAll({String? search, TaskStatus? status}) async {
    try {
      final response = await _client.dio.get<Map<String, dynamic>>(
        '/api/tasks',
        queryParameters: {
          if (search != null && search.trim().isNotEmpty)
            'search': search.trim(),
          if (status != null) 'status': status.apiValue,
          'page': 0,
          'size': 100,
          'sort': 'createdAt,desc',
        },
      );
      final content = response.data!['content'] as List<dynamic>;
      return content
          .map((json) => Task.fromJson(json as Map<String, dynamic>))
          .toList();
    } catch (error) {
      throw ApiClient.mapError(error);
    }
  }

  Future<Task> create(Task task) async {
    try {
      final response = await _client.dio.post<Map<String, dynamic>>(
        '/api/tasks',
        data: task.toRequest(),
      );
      return Task.fromJson(response.data!);
    } catch (error) {
      throw ApiClient.mapError(error);
    }
  }

  Future<Task> update(Task task, {TaskStatus? status}) async {
    try {
      final response = await _client.dio.put<Map<String, dynamic>>(
        '/api/tasks/${task.id}',
        data: task.toRequest(statusOverride: status),
      );
      return Task.fromJson(response.data!);
    } catch (error) {
      throw ApiClient.mapError(error);
    }
  }

  Future<void> delete(String id) async {
    try {
      await _client.dio.delete<void>('/api/tasks/$id');
    } catch (error) {
      throw ApiClient.mapError(error);
    }
  }
}
