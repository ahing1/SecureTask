CREATE TABLE tasks (
   id BIGSERIAL PRIMARY KEY,
   title VARCHAR(100) NOT NULL,
   description TEXT,
   status VARCHAR(50) NOT NULL,
   priority VARCHAR(50) NOT NULL,
   due_date DATE,
   creator_id BIGINT REFERENCES users(id),
   assignee_id BIGINT REFERENCES users(id),
   organization_id BIGINT REFERENCES organizations(id),
   created_at TIMESTAMP DEFAULT NOW()
);