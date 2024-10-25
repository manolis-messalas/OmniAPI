-- Insert authors
INSERT INTO author (NAME, DATE_OF_BIRTH, COUNTRY_OF_ORIGIN)
VALUES
    ('Erich Fromm', '23 March 1900', 'German'),
    ('Bill Bryson', '8 December 1951', 'USA'),
    ('Fyodor Dostoevsky', '11 November 1821', 'Russia'),
    ('Konstantinos Kavafis', '29 April 1863', 'Greece'),
    ('Tim Marshall', '1 May 1959', 'UK'),
    ('Yuval Noah Harari', '24 February 1976', 'Israel'),
    ('Walter Kaufmann', '1 July 1921', 'Germany');


-- Insert books
INSERT INTO book (NAME, PUBLICATION_YEAR, AUTHOR_ID)
VALUES
    ('The Fear of Freedom', '1941', 1),
    ('The Body', '2021', 2),
    ('Crime and Punishment', '1865', 3),
    ('The Poems', '1963', 4),
    ('Prisoners of Geography', '2015', 5),
    ('Sapiens: A Brief History of Humankind', '2015', 6),
    ('On the Genealogy of Morals and Ecce Homo', '1989', 7);

