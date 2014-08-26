package pconley.vamp.source;

import java.util.List;

import pconley.vamp.model.Track;

public interface TrackSource {

	public List<Track> getAll();

	public void insert();
}
