import 'package:dio/dio.dart';

import '../config/app_config.dart';
import '../storage/token_storage.dart';
import 'api_exception.dart';

class ApiClient {
  ApiClient({required this.tokenStorage, Dio? dio})
    : dio =
          dio ??
          Dio(
            BaseOptions(
              baseUrl: AppConfig.apiUrl,
              connectTimeout: const Duration(seconds: 10),
              receiveTimeout: const Duration(seconds: 10),
              headers: const {'Content-Type': 'application/json'},
            ),
          ) {
    this.dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) async {
          final token = await tokenStorage.read();
          if (token != null) {
            options.headers['Authorization'] = 'Bearer $token';
          }
          handler.next(options);
        },
      ),
    );
  }

  final Dio dio;
  final TokenStorage tokenStorage;

  static ApiException mapError(Object error) {
    if (error is ApiException) return error;
    if (error is! DioException) {
      return const ApiException('Une erreur inattendue est survenue.');
    }
    final data = error.response?.data;
    if (data is Map<String, dynamic>) {
      final message = data['message'];
      if (message is String && message.isNotEmpty) {
        return ApiException(message, statusCode: error.response?.statusCode);
      }
    }
    if (error.type == DioExceptionType.connectionError ||
        error.type == DioExceptionType.connectionTimeout) {
      return const ApiException(
        'Impossible de joindre le serveur. Verifiez que l\'API est demarree.',
      );
    }
    return ApiException(
      'La requete a echoue.',
      statusCode: error.response?.statusCode,
    );
  }
}
