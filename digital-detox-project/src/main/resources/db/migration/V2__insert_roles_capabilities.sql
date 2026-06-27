INSERT INTO roles (name)
VALUES ('ADMIN'),
       ('COACH'),
       ('MEMBER');

INSERT INTO capabilities (name, description)
VALUES ('VIEW_OWN_PROFILE', 'View own user profile'),
       ('EDIT_OWN_PROFILE', 'Edit own user profile'),
       ('VIEW_MEMBERS', 'View member profiles'),
       ('VIEW_COACHES', 'View coach profiles'),
       ('APPROVE_COACH', 'Approve pending coach accounts'),
       ('CREATE_PLAN', 'Create detox plans'),
       ('VIEW_PLANS', 'View all detox plans'),
       ('VIEW_OWN_PLANS', 'View own detox plans'),
       ('EDIT_PLAN', 'Edit detox plans'),
       ('CREATE_CHECKIN', 'Submit daily check-ins'),
       ('VIEW_CHECKINS', 'View check-ins for assigned plans'),
       ('VIEW_OWN_CHECKINS', 'View own check-ins'),
       ('CREATE_REVIEW', 'Create weekly reviews'),
       ('VIEW_REVIEWS', 'View weekly reviews'),
       ('MANAGE_USERS', 'Manage platform users');

INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
         JOIN capabilities c ON r.name = 'ADMIN';

INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
         JOIN capabilities c ON r.name = 'COACH'
WHERE c.name IN (
                 'VIEW_OWN_PROFILE', 'EDIT_OWN_PROFILE', 'VIEW_MEMBERS',
                 'CREATE_PLAN', 'VIEW_PLANS', 'EDIT_PLAN',
                 'VIEW_CHECKINS', 'CREATE_REVIEW', 'VIEW_REVIEWS'
    );

INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
         JOIN capabilities c ON r.name = 'MEMBER'
WHERE c.name IN (
                 'VIEW_OWN_PROFILE', 'EDIT_OWN_PROFILE',
                 'VIEW_OWN_PLANS', 'CREATE_CHECKIN', 'VIEW_OWN_CHECKINS'
    );
