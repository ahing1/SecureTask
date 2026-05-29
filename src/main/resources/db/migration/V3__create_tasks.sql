CREATE TABLE tasks (
   id SERIAL PRIMARY KEY,
   title VARCHAR(100) NOT NULL,
   description TEXT,
   status VARCHAR(50) NOT NULL,
   priority VARCHAR(50) NOT NULL,
   due_date DATE,
   creator_id INT REFERENCES users(id),
   assignee_id INT REFERENCES users(id),
   organization_id INT REFERENCES organizations(id),
   created_at TIMESTAMP DEFAULT NOW()
);