SELECT m.id, u.display_name, m.role FROM users u JOIN project_members m ON u.id = m.user_id;
