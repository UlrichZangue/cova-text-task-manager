ALTER TABLE users
    ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    MODIFY COLUMN name VARCHAR(100) NOT NULL,
    MODIFY COLUMN password VARCHAR(60) NOT NULL;

ALTER TABLE tasks
    MODIFY COLUMN title VARCHAR(150) NOT NULL;

CREATE UNIQUE INDEX idx_users_email ON users (email);
CREATE INDEX idx_tasks_user_id ON tasks (user_id);
CREATE INDEX idx_tasks_status ON tasks (status);
CREATE INDEX idx_tasks_priority ON tasks (priority);
CREATE INDEX idx_tasks_deleted_at ON tasks (deleted_at);
