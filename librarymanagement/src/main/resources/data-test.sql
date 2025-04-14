-- Insert test data for users
INSERT INTO users (id, name, email, password, address, phone, role) 
VALUES 
(1, 'Test User', 'testuser@example.com', '$2a$10$eDhNCmQES.hZgz0UqkDDmOh3FENZ9/ZDGdvmMft3BsWgsCdC5MnIi', '123 Test St', '1234567890', 'USER'),
(2, 'Test Admin', 'testadmin@example.com', '$2a$10$eDhNCmQES.hZgz0UqkDDmOh3FENZ9/ZDGdvmMft3BsWgsCdC5MnIi', '456 Admin St', '0987654321', 'ADMIN'),
(3, 'Test Librarian', 'testlibrarian@example.com', '$2a$10$eDhNCmQES.hZgz0UqkDDmOh3FENZ9/ZDGdvmMft3BsWgsCdC5MnIi', '789 Library St', '5555555555', 'LIBRARIAN');

-- Insert test data for books
INSERT INTO books (id, title, author, isbn, publisher, publication_date, category, total_copies, available_copies, available) 
VALUES 
(1, 'Test Book 1', 'Author 1', '1234567890', 'Test Publisher', '2023-01-01', 'Fiction', 3, 3, true),
(2, 'Test Book 2', 'Author 2', '0987654321', 'Test Publisher', '2023-02-01', 'Non-Fiction', 2, 1, true),
(3, 'Test Book 3', 'Author 3', '1122334455', 'Another Publisher', '2023-03-01', 'Science', 1, 0, false);

-- Insert test data for transactions
INSERT INTO transactions (id, book_id, user_id, issue_date, due_date, return_date, status, fine) 
VALUES 
(1, 2, 1, '2023-06-01', '2023-06-15', NULL, 'ISSUED', NULL),
(2, 3, 1, '2023-05-15', '2023-05-29', NULL, 'OVERDUE', 2.5);