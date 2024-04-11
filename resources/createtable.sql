CREATE DATABASE moviedb;

CREATE TABLE moviedb.movies (
    id VARCHAR(10) PRIMARY KEY,
    title VARCHAR(100) NOT NULL DEFAULT '',
    year INTEGER NOT NULL,
    director VARCHAR(100) NOT NULL DEFAULT ''
);

CREATE TABLE moviedb.stars (
    id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL DEFAULT '',
    birthYear INTEGER
);

CREATE TABLE moviedb.stars_in_movies (
    starId VARCHAR(10) NOT NULL DEFAULT '',
    movieId VARCHAR(10) NOT NULL DEFAULT '',
    PRIMARY KEY (starId, movieId),
    FOREIGN KEY (starId) REFERENCES stars(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE moviedb.genres (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(32) NOT NULL DEFAULT ''
);

CREATE TABLE moviedb.genres_in_movies (
    genreId INTEGER NOT NULL,
    movieId VARCHAR(10) NOT NULL DEFAULT '',
    PRIMARY KEY (genreId, movieId),
    FOREIGN KEY (genreId) REFERENCES genres(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE moviedb.creditcards (
    id VARCHAR(20) PRIMARY KEY,
    firstName VARCHAR(50) NOT NULL DEFAULT '',
    lastName VARCHAR(50) NOT NULL DEFAULT '',
    expiration DATE NOT NULL
);


CREATE TABLE moviedb.customers (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    firstName VARCHAR(50) NOT NULL DEFAULT '',
    lastName VARCHAR(50) NOT NULL DEFAULT '',
    ccId VARCHAR(20) NOT NULL DEFAULT '',
    address VARCHAR(200) NOT NULL DEFAULT '',
    email VARCHAR(50) NOT NULL DEFAULT '',
    password VARCHAR(20) NOT NULL DEFAULT '',
    FOREIGN KEY (ccId) REFERENCES creditcards(id)
);

CREATE TABLE moviedb.sales (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    customerId INTEGER NOT NULL,
    movieId VARCHAR(10) NOT NULL DEFAULT '',
    saleDate DATE NOT NULL,
    FOREIGN KEY (customerId) REFERENCES customers(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE moviedb.ratings (
    movieId VARCHAR(10) NOT NULL DEFAULT '',
    rating FLOAT NOT NULL,
    numVotes INTEGER NOT NULL,
    PRIMARY KEY (movieId),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

