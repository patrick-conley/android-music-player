A music player for Android.

The primary goal is to allow me to navigate my own music collection, which
requires much more generic handling than is supported by the players I've
seen. Unique features are (or rather, will be)

- a custom tag-reading library able to read and store Vorbis Comments, in
  which tags are arbitrary, and may be repeated. Porting Mutagen or another
  library is a possibility.
- a player UI customizable at runtime so custom tags can be shown (something
  in the manner of Quod Libet or Rockbox).
- a flexible filtering interface allowing music to be searched for rapidly
  according to custom tags. For example, (GENRE = Classical -> GENRE =
  Symphony -> COMPOSER = BEETHOVEN -> GROUPING = ???). Filters should be saved
  so navigation is quick after the initial setup.
- a shuffling algorithm based on the filters, for example "shuffle by
  grouping" after the search above.

Support for ID3 and other tag formats is considered a secondary objective, and
will probably be handled by Android's built-in library. Other common features
of music players, like playlists, tag editing, or album art support, are
effectively off the table.
