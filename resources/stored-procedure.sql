DELIMITER //

CREATE PROCEDURE add_movie(
    IN movie_title VARCHAR(100),
    IN movie_year INT,
    IN movie_director VARCHAR(50),
    IN star_name VARCHAR(100),
    IN genre_name VARCHAR(100)
)
add_movie_proc: BEGIN
    DECLARE star_id VARCHAR(10);
    DECLARE genre_id INT;
    DECLARE movie_id VARCHAR(10);
    DECLARE movie_id_check VARCHAR(10);

        -- Check if the movie title already exists
    SELECT id INTO movie_id_check FROM movies WHERE title = movie_title AND director = movie_director AND `year` = movie_year;
    IF movie_id_check IS NOT NULL THEN
            -- Movie title is a duplicate, end the procedure

            LEAVE add_movie_proc;
    END IF;

        -- Check if the star exists, if not, insert it
    SELECT id INTO star_id FROM stars WHERE name = star_name LIMIT 1;
    IF star_id IS NULL THEN
            SET star_id = (
                SELECT CONCAT('nm', COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) + 1, 1)) FROM stars
            );
    INSERT INTO stars (id, name) VALUES (star_id, star_name);
    END IF;

        -- Check if the genre exists, if not, insert it
    SELECT id INTO genre_id FROM genres WHERE name = genre_name LIMIT 1;
    IF genre_id IS NULL THEN
            INSERT INTO genres (name) VALUES (genre_name);
            SET genre_id = LAST_INSERT_ID();
    END IF;

        -- Generate a new movie ID that doesn't conflict with existing IDs
        -- Use a transaction to ensure atomicity and avoid race conditions
    START TRANSACTION;
    SELECT CONCAT('tt', COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) + 1, 1)) INTO movie_id FROM movies FOR UPDATE;
    INSERT INTO movies (id, title, `year`, director) VALUES (movie_id, movie_title, movie_year, movie_director);
    COMMIT;

    -- Associate the star with the movie
    INSERT INTO stars_in_movies (starId, movieId) VALUES (star_id, movie_id);

    -- Associate the genre with the movie
    INSERT INTO genres_in_movies (genreId, movieId) VALUES (genre_id, movie_id);

END //

DELIMITER ;
