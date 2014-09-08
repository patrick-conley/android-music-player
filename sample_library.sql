/*
 * SQL input to build the tables Tracks, Tags, TrackHasTags using some music
 * from my library.
 *
 * To rebuild *an existing database*, do
 *     cat sample_library.sql | sqlite3 library.db
 */
delete from Tracks;
delete from Tags;
delete from TrackHasTags;

insert into Tracks (_id, trackUri) values
(1, "file:///sdcard/Music/They Might Be Giants/They Might Be Giants/01 Everything Right Is Wrong Again.m4a"),
(2, "file:///sdcard/Music/They Might Be Giants/They Might Be Giants/02 Put Your Hand Inside the Puppet Head.m4a"),
(3, "file:///sdcard/Music/They Might Be Giants/They Might Be Giants/03 Number Three.m4a"),
(4, "file:///sdcard/Music/They Might Be Giants/They Might Be Giants/17 Alienation's for the Rich.m4a"),
(5, "file:///sdcard/Music/They Might Be Giants/They Might Be Giants/18 The Day.m4a"),
(6, "file:///sdcard/Music/They Might Be Giants/They Might Be Giants/19 Rhythm Section Want Ad.m4a");

/* These tags aren't being inserted in the correct order, but inserting them
 * out-of-order makes life simple, and shouldn't be problematic.
 */
insert into Tags (_id, name, value) values
(1, "ARTIST", "They Might Be Giants"),
(2, "COMPOSER", "They Might Be Giants"),
(3, "ALBUM", "They Might Be Giants"),
(4, "GENRE", "Rock - Alternative"),
(5, "DISKNUMBER", "1"),
(6, "YEAR", "1986"),
(7, "ALBUMARTIST", "They Might Be Giants"),

(8, "TITLE", "Everything Right Is Wrong Again"),
(9, "TITLE", "Put Your Hand Inside the Puppet Head"),
(10, "TITLE", "Number Three"),
(11, "TITLE", "Alienation's for the Rich"),
(12, "TITLE", "The Day"),
(13, "TITLE", "Rhythm Section Want Ad"),

(14, "TRACKNUMBER", "1"),
(15, "TRACKNUMBER", "2"),
(16, "TRACKNUMBER", "3"),
(17, "TRACKNUMBER", "17"),
(18, "TRACKNUMBER", "18"),
(19, "TRACKNUMBER", "19");

insert into TrackHasTags (trackID, tagID) values
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 14),
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 9), (2, 15),
(3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 10), (3, 16),
(4, 1), (4, 2), (4, 3), (4, 4), (4, 5), (4, 6), (4, 7), (4, 11), (4, 17),
(5, 1), (5, 2), (5, 3), (5, 4), (5, 5), (5, 6), (5, 7), (5, 12), (5, 18),
(6, 1), (6, 2), (6, 3), (6, 4), (6, 5), (6, 6), (6, 7), (6, 13), (6, 19);

insert into Tracks (_id, trackUri) values
(7, "file:///sdcard/Music/Schoenberg, Arnold/Concerto for Violin and Orchestra/00-01 Poco allegro.ogg"),
(8, "file:///sdcard/Music/Schoenberg, Arnold/Concerto for Violin and Orchestra/00-02 Andante grazioso.ogg"),
(9, "file:///sdcard/Music/Schoenberg, Arnold/Concerto for Violin and Orchestra/00-03 Finale Allegro.ogg"),
(10, "file:///sdcard/Music/Sibelius, Jean/Concerto for Violin and Orchestra in D minor/00-04 Allegro moderato.ogg"),
(11, "file:///sdcard/Music/Sibelius, Jean/Concerto for Violin and Orchestra in D minor/00-05 Adagio di molto.ogg"),
(12, "file:///sdcard/Music/Sibelius, Jean/Concerto for Violin and Orchestra in D minor/00-06 Allegro, ma non tanto.ogg");

insert into Tags (_id, name, value) values
(20, "CONDUCTOR", "Esa-Pekka Salonen"),
(21, "GENRE", "Violin"),
(22, "GENRE", "Concerto"),
(23, "PERFORMER", "Hilary Hahn (Violin)"),
(24, "DATE", "2008-01-01"),
(25, "ALBUM", "Schoenberg/Sibelius · Violin Concertos"),
(26, "ENSEMBLE", "Swedish Radio Symphony Orchestra"),
(27, "ARTIST", "Hilary Hahn/Swedish Radio Symph. Orch."),
(28, "LABEL", "Deutsch Grammophon"),

(29, "GENRE", "20th Century"),
(30, "COMPOSER", "Schoenberg, Arnold"),
(31, "TITLE", "Concerto for Violin and Orchestra"),
(32, "OPUS", "36"),
(33, "PART", "Poco allegro"),
(34, "PART", "Andante grazioso"),
(35, "PART", "Finale Allegro"),

(36, "GENRE", "Late Romantic"),
(37, "COMPOSER", "Sibelius, Jean"),
(38, "TITLE", "Concerto for Violin and Orchestra in D minor"),
(39, "OPUS", "47"),
(40, "PART", "Allegro moderato"),
(41, "PART", "Adagio di molto"),
(42, "PART", "Allegro, ma non tanto"),

(43, "TRACKNUMBER", "4"),
(44, "TRACKNUMBER", "5"),
(45, "TRACKNUMBER", "6");

insert into TrackHasTags (trackID, tagID) values
(7, 20), (7, 21), (7, 22), (7, 23), (7, 24), (7, 25), (7, 26), (7, 27), (7, 28), (7, 29), (7, 30), (7, 31), (7, 32), (7, 33), (7, 14),
(8, 20), (8, 21), (8, 22), (8, 23), (8, 24), (8, 25), (8, 26), (8, 27), (8, 28), (8, 29), (8, 30), (8, 31), (8, 32), (8, 34), (8, 15),
(9, 20), (9, 21), (9, 22), (9, 23), (9, 24), (9, 25), (9, 26), (9, 27), (9, 28), (9, 29), (9, 30), (9, 31), (9, 32), (9, 35), (9, 16),
(10, 20), (10, 21), (10, 22), (10, 23), (10, 24), (10, 25), (10, 26), (10, 27), (10, 28), (10, 36), (10, 37), (10, 38), (10, 39), (10, 40), (10, 43),
(11, 20), (11, 21), (11, 22), (11, 23), (11, 24), (11, 25), (11, 26), (11, 27), (11, 28), (11, 36), (11, 37), (11, 38), (11, 39), (11, 41), (11, 44),
(12, 20), (12, 21), (12, 22), (12, 23), (12, 24), (12, 25), (12, 26), (12, 27), (12, 28), (12, 36), (12, 37), (12, 38), (12, 39), (12, 42), (12, 45);

