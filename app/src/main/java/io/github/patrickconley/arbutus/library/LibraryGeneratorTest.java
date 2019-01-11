package io.github.patrickconley.arbutus.library;

import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

public class LibraryGeneratorTest {

    /**
     * Builds a library
     *
     * root "Library"
     *  -> Classical
     *  -> Popular
     *
     * Classical "Composers"
     *  -> Fairouz, Mohammed
     *  -> Koehne, Graham
     *
     * Fairouz, Mohammed "Works"
     *  -> Posh,
     * @return
     */
    public static LibraryNode buildSampleLibrary() {

        LibraryNode root = new LibraryNode("root", "Library");

        LibraryNode classicalComposers = new LibraryNode("Classical", "Composers");
        root.children.add(classicalComposers);
        classicalComposers.children.add(buildFairouz());
        classicalComposers.children.add(buildKoehne());

        LibraryNode popularArtists = new LibraryNode("Popular", "Artists");
        root.children.add(popularArtists);
        popularArtists.children.add(buildTMBG());

        return root;
    }

    @NonNull
    private static LibraryNode buildTMBG() {
        LibraryNode artist = new LibraryNode("They Might Be Giants", "Albums");

        LibraryNode album = new LibraryNode("Glean", "Tracks");
        album.children.add(new TrackNode("Erase", 10));
        album.children.add(new TrackNode("Good to Be Alive", 11));
        return artist;
    }

    // composers -> works -> movements
    private static LibraryNode buildFairouz() {
        LibraryNode composer = new LibraryNode("Fairouz, Mohammed", "Works");

        LibraryNode work = new LibraryNode("Posh", "Movements");
        work.children.add(new TrackNode("Ballade of the Layette", 1));
        work.children.add(new TrackNode("Blue Sea Songs", 2));
        work.children.add(new TrackNode("Posh", 3));
        composer.children.add(work);

        work = new LibraryNode("For Victims", "Movements");
        work.children.add(new TrackNode("Prologue: The House of Justice", 4));
        work.children.add(new TrackNode("Song of the Victims", 5));
        composer.children.add(work);

        return composer;
    }

    // composers -> works -> movements
    private static LibraryNode buildKoehne() {
        LibraryNode composer = new LibraryNode("Koehne, Graham", "Works");

        LibraryNode work = new LibraryNode("Elevator Music",  "Movements");
        work.children.add(new TrackNode("Elevator Music", 6));

        work = new LibraryNode("Inflight Entertainment", "Movements");
        work.children.add(new TrackNode("Agent Provocateur", 7));
        work.children.add(new TrackNode("Horse Opera", 8));
        work.children.add(new TrackNode("Beat Girl", 9));

        return composer;
    }

    private static class LibraryNode {
        private String name;
        private String title;
        private Set<LibraryNode> children = new HashSet<>();

        public LibraryNode(String name, String title) {
            this.name = name;
            this.title = title;
        }
    }

    private static class TrackNode extends LibraryNode {
        private long trackId;

        public TrackNode(String name, long trackId) {
            super(name, null);
            this.trackId = trackId;
        }
    }
}
