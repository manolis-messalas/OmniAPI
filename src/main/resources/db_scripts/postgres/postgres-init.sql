-- PostgreSQL initialization script for OmniAPI (postgres subfolder)
-- This script is automatically executed by PostgreSQL container on first startup when the data directory is empty

-- Insert Authors
INSERT INTO author (name, date_of_birth, country_of_origin) VALUES
('Erich Fromm', '23 March 1900', 'German');
INSERT INTO author (name, date_of_birth, country_of_origin) VALUES
('Bill Bryson', '8 December 1951', 'USA');
INSERT INTO author (name, date_of_birth, country_of_origin) VALUES
('Fyodor Dostoevsky', '11 November 1821', 'Russia');
INSERT INTO author (name, date_of_birth, country_of_origin) VALUES
('Konstantinos Kavafis', '29 April 1863', 'Greece');
INSERT INTO author (name, date_of_birth, country_of_origin) VALUES
('Tim Marshall', '1 May 1959', 'UK');
INSERT INTO author (name, date_of_birth, country_of_origin) VALUES
('Yuval Noah Harari', '24 February 1976', 'Israel');

-- Insert Books
INSERT INTO book (name, publication_year, author_id) VALUES
('The Fear of Freedom', '1941', 1);
INSERT INTO book (name, publication_year, author_id) VALUES
('The Body', '2021', 2);
INSERT INTO book (name, publication_year, author_id) VALUES
('Crime and Punishment', '1865', 3);
INSERT INTO book (name, publication_year, author_id) VALUES
('The Poems', '1963', 4);
INSERT INTO book (name, publication_year, author_id) VALUES
('Prisoners of Geography', '2015', 5);
INSERT INTO book (name, publication_year, author_id) VALUES
('Sapiens: A Brief History of Humankind', '2015', 6);

