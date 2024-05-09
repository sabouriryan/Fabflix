DELIMITER //

CREATE PROCEDURE add_movie(
    IN movie_title VARCHAR(100),
    IN movie_year INT,
    IN movie_director VARCHAR(50),
    IN star_name VARCHAR(100),
    IN genre_name VARCHAR(100)
)
BEGIN
    DECLARE star_id INT;
    DECLARE genre_id INT;
    DECLARE movie_id INT;

    -- Check if the movie title already exists
    IF EXISTS (SELECT 1 FROM movies WHERE title = movie_title) THEN
        -- Movie title is a duplicate, end the procedure
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Duplicate movie title. Aborting procedure.';
        RETURN;
    END IF;

    -- Check if the star exists, if not, insert it
    SELECT id INTO star_id FROM stars WHERE name = star_name;
    IF star_id IS NULL THEN
        SET star_id = (
            SELECT CONCAT('nm', COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) + 1, 1)) FROM stars;
        );
        INSERT INTO stars (id, name) VALUES (star_id, star_name);
        SET star_id = LAST_INSERT_ID();
    END IF;

    -- Check if the genre exists, if not, insert it
    -- we dont need to generate an id for insertion because genre_id has auto increment
    SELECT id INTO genre_id FROM genres WHERE name = genre_name;
    IF genre_id IS NULL THEN
        INSERT INTO genres (name) VALUES (genre_name);
        SET genre_id = LAST_INSERT_ID();
    END IF;

     -- Generate a new movie ID that doesn't conflict with existing IDs
     -- this solves the "what if the id already exists" problem
     -- this is slow but should be fine because movie insertions arent happening too often
    SET movie_id = (
        SELECT CONCAT('tt', COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) + 1, 1)) FROM movies;
    );

    -- Insert the movie
    INSERT INTO movies (id, title, `year`, director) VALUES (movie_id, movie_title, movie_year, movie_director);
    SET movie_id = LAST_INSERT_ID();

    -- Associate the star with the movie
    INSERT INTO stars_in_movies (starId, movieId) VALUES (star_id, movie_id);

    -- Associate the genre with the movie
    INSERT INTO genres_in_movies (genreId, movieId) VALUES (genre_id, movie_id);
    
    -- Output the ID of the newly inserted movie
    SELECT movie_id;
END //

DELIMITER ;
