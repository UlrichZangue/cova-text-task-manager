import 'package:flutter/material.dart';

import 'auth_controller.dart';

class RegisterScreen extends StatefulWidget {
  const RegisterScreen({required this.controller, super.key});

  final AuthController controller;

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final _formKey = GlobalKey<FormState>();
  final _name = TextEditingController();
  final _email = TextEditingController();
  final _password = TextEditingController();
  final _confirmation = TextEditingController();

  @override
  void dispose() {
    _name.dispose();
    _email.dispose();
    _password.dispose();
    _confirmation.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    final success = await widget.controller.register(
      _name.text,
      _email.text,
      _password.text,
    );
    if (success && mounted) Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Creer un compte')),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 440),
              child: ListenableBuilder(
                listenable: widget.controller,
                builder: (context, _) => Form(
                  key: _formKey,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      TextFormField(
                        controller: _name,
                        textCapitalization: TextCapitalization.words,
                        decoration: const InputDecoration(
                          labelText: 'Nom',
                          prefixIcon: Icon(Icons.person_outline),
                        ),
                        validator: (value) =>
                            value == null || value.trim().isEmpty
                            ? 'Le nom est obligatoire.'
                            : null,
                      ),
                      const SizedBox(height: 16),
                      TextFormField(
                        controller: _email,
                        keyboardType: TextInputType.emailAddress,
                        decoration: const InputDecoration(
                          labelText: 'Email',
                          prefixIcon: Icon(Icons.mail_outline),
                        ),
                        validator: (value) =>
                            value == null || !value.contains('@')
                            ? 'Saisissez une adresse email valide.'
                            : null,
                      ),
                      const SizedBox(height: 16),
                      TextFormField(
                        controller: _password,
                        obscureText: true,
                        decoration: const InputDecoration(
                          labelText: 'Mot de passe',
                          prefixIcon: Icon(Icons.lock_outline),
                        ),
                        validator: (value) => value == null || value.length < 8
                            ? 'Utilisez au moins 8 caracteres.'
                            : null,
                      ),
                      const SizedBox(height: 16),
                      TextFormField(
                        controller: _confirmation,
                        obscureText: true,
                        decoration: const InputDecoration(
                          labelText: 'Confirmer le mot de passe',
                          prefixIcon: Icon(Icons.lock_reset),
                        ),
                        validator: (value) => value != _password.text
                            ? 'Les mots de passe ne correspondent pas.'
                            : null,
                      ),
                      if (widget.controller.error != null) ...[
                        const SizedBox(height: 16),
                        Text(
                          widget.controller.error!,
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            color: Theme.of(context).colorScheme.error,
                          ),
                        ),
                      ],
                      const SizedBox(height: 24),
                      FilledButton.icon(
                        onPressed: widget.controller.isLoading ? null : _submit,
                        icon: widget.controller.isLoading
                            ? const SizedBox.square(
                                dimension: 18,
                                child: CircularProgressIndicator(
                                  strokeWidth: 2,
                                ),
                              )
                            : const Icon(Icons.person_add_alt),
                        label: const Text('Creer mon compte'),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
