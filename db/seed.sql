-- Demo catalog only. Users are inserted by SeedService with bcrypt hashes.
-- Re-run is safe: MERGE / known slugs.

MERGE INTO categories (id, name, slug) KEY (id) VALUES
    (1, 'Electronics', 'electronics'),
    (2, 'Books', 'books'),
    (3, 'Home', 'home'),
    (4, 'Fashion', 'fashion'),
    (5, 'Sports', 'sports');
