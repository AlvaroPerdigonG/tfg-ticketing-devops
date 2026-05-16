-- Crear tabla de usuarios (sin password_hash todavía)
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    display_name VARCHAR(120) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true
);

-- Crear tabla de categorías
CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT true
);

-- Crear tabla de tickets
CREATE TABLE tickets (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by_user_id UUID NOT NULL REFERENCES users(id),
    category_id UUID NOT NULL REFERENCES categories(id),
    assigned_to_user_id UUID REFERENCES users(id)
);

-- Crear tabla de comentarios
CREATE TABLE ticket_comments (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    author_user_id UUID NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Crear índices para mejorar el rendimiento
CREATE INDEX idx_tickets_created_by ON tickets(created_by_user_id);
CREATE INDEX idx_tickets_assigned_to ON tickets(assigned_to_user_id);
CREATE INDEX idx_tickets_category ON tickets(category_id);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_ticket_comments_ticket ON ticket_comments(ticket_id);
CREATE INDEX idx_ticket_comments_author ON ticket_comments(author_user_id);

-- Insertar datos iniciales
INSERT INTO users (id, email, display_name, role, is_active) VALUES
    ('550e8400-e29b-41d4-a716-446655440001'::UUID, 'user@local.test', 'Test User', 'USER', true),
    ('550e8400-e29b-41d4-a716-446655440002'::UUID, 'agent@local.test', 'Test Agent', 'AGENT', true);

INSERT INTO categories (id, name, is_active) VALUES
    ('650e8400-e29b-41d4-a716-446655440001'::UUID, 'Technical Support', true),
    ('650e8400-e29b-41d4-a716-446655440002'::UUID, 'Billing', true),
    ('650e8400-e29b-41d4-a716-446655440003'::UUID, 'General Inquiry', true);

