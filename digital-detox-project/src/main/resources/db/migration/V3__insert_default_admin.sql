INSERT INTO capabilities (name, description)
VALUES ('UPLOAD_ATTACHMENT', 'Upload files to daily check-ins')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
         JOIN capabilities c ON c.name = 'UPLOAD_ATTACHMENT'
WHERE r.name IN ('ADMIN', 'MEMBER');

INSERT INTO users (uuid, username, email, password, role_id, is_active, created_at, updated_at, deleted)
SELECT gen_random_uuid(),
       'admin',
       'admin@digitaldetox.local',
       '$2a$12$zYXkjE.GrNWxgFIr5fGVN.nTqSTy239ZNvb6kaEnBKexqedzX9Y/K',
       r.id,
       TRUE,
       NOW(),
       NOW(),
       FALSE
FROM roles r
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM users u WHERE u.username = 'admin');
