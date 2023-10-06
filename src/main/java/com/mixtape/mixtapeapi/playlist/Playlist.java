package com.mixtape.mixtapeapi.playlist;

import com.mixtape.mixtapeapi.mixtape.Mixtape;
import com.mixtape.mixtapeapi.profile.Profile;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String spotifyID;
    private String name;

    @ManyToOne
    @JoinColumn(name="initiator_id")
    private Profile initiator;

    @ManyToOne
    @JoinColumn(name="target_id")
    private Profile target;
    private String description;
    private String coverPicURL;

    @OneToMany
    private List<Mixtape> mixtapes;

    public Playlist() {
    }

    public Playlist(String id, String spotifyID, String name, Profile initiator, Profile target, String description, String coverPicURL) {
        this.id = id;
        this.spotifyID = spotifyID;
        this.name = name;
        this.initiator = initiator;
        this.target = target;
        this.description = description;
        this.coverPicURL = coverPicURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSpotifyID() {
        return spotifyID;
    }

    public void setSpotifyID(String spotifyID) {
        this.spotifyID = spotifyID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Profile getInitiator() {
        return initiator;
    }

    public void setInitiator(Profile initiator) {
        this.initiator = initiator;
    }

    public Profile getTarget() {
        return target;
    }

    public void setTarget(Profile target) {
        this.target = target;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverPicURL() {
        return coverPicURL;
    }

    public void setCoverPicURL(String coverPicURL) {
        this.coverPicURL = coverPicURL;
    }

    public List<Mixtape> getMixtapes() {
        return mixtapes;
    }

    public void setMixtapes(List<Mixtape> mixtapes) {
        mixtapes.forEach(mixtape -> mixtape.setParentPlaylistId(this.id));
        this.mixtapes = mixtapes;
    }

    public void addMixtape(Mixtape mixtape) {
        mixtape.setParentPlaylistId(this.id);
        this.mixtapes.add(mixtape);
    }
}

