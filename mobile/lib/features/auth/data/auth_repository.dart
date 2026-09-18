import '../../../core/network/api_client.dart';

class AuthRepository {
  const AuthRepository(this._client);

  final ApiClient _client;

  Future<String> login({
    required String email,
    required String password,
  }) async {
    try {
      final response = await _client.dio.post<Map<String, dynamic>>(
        '/api/auth/login',
        data: {'email': email.trim(), 'password': password},
      );
      return response.data!['token'] as String;
    } catch (error) {
      throw ApiClient.mapError(error);
    }
  }

  Future<void> register({
    required String name,
    required String email,
    required String password,
  }) async {
    try {
      await _client.dio.post<void>(
        '/api/auth/register',
        data: {
          'name': name.trim(),
          'email': email.trim(),
          'password': password,
          'confirmPassword': password,
        },
      );
    } catch (error) {
      throw ApiClient.mapError(error);
    }
  }
}
