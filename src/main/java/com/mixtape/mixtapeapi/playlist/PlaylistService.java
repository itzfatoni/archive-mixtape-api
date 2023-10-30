package com.mixtape.mixtapeapi.playlist;

import com.mixtape.mixtapeapi.invitation.InvitationService;
import com.mixtape.mixtapeapi.profile.Profile;
import com.mixtape.mixtapeapi.tracks.TrackService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final TrackService trackService;
    private final InvitationService invitationService;
    private final PlaylistPicUploadService pictureUploadService;

    public PlaylistService(PlaylistRepository playlistRepository, TrackService trackService, InvitationService invitationService, PlaylistPicUploadService pictureUploadService) {
        this.playlistRepository = playlistRepository;
        this.trackService = trackService;
        this.invitationService = invitationService;
        this.pictureUploadService = pictureUploadService;
    }

    public Optional<Playlist> findPlaylist(String id) {
        return playlistRepository.findById(id);
    }

    public Optional<Playlist> findPlaylistForProfile(Profile profile, String playlistId) throws IOException {
        Optional<Playlist> playlistOpt = playlistRepository.findByIdAndInitiatorOrTarget(playlistId, profile, profile);
        if (playlistOpt.isEmpty()) {
            return playlistOpt;
        }

        Playlist inflatedPlaylist = trackService.inflatePlaylist(playlistOpt.get());
        return Optional.of(inflatedPlaylist);
    }

    public List<Playlist> findPlaylistsForProfile(Profile profile) throws IOException {
        List<Playlist> playlists = playlistRepository.findByInitiatorOrTarget(profile, profile);
        for (var playlist : playlists) {
            trackService.inflatePlaylist(playlist);
        }

        return playlists;
    }

    public Playlist createPlaylist(Profile initiator, PlaylistDTO.Create createPlaylist, Profile requestedTarget) {
        // Create the playlist
        Playlist playlist = new Playlist(null, "", createPlaylist.name, initiator, null, createPlaylist.description, createPlaylist.coverPicURL);
        playlist = savePlaylist(playlist);

        // create an additional invitation for the playlist
        invitationService.createInvitationFromPlaylist(playlist, requestedTarget);

        return playlist;
    }

    public Playlist acceptPlaylistInvitation(Profile acceptor, String playlistId) {
        Playlist requestedPlaylist = this.findPlaylist(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        requestedPlaylist.setTarget(acceptor);
        return savePlaylist(requestedPlaylist);
    }

    public Playlist savePlaylist(Playlist playlist) {
        return playlistRepository.save(playlist);
    }

    public void removePlaylist(String playlistId) {
        this.playlistRepository.deleteById(playlistId);
    }

    public Playlist setPlaylistPicture(String playlistId, MultipartFile picture) throws IOException {
        Playlist requestedPlaylist = findPlaylist(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested playlist does not exist"));

        String pictureURL = pictureUploadService.uploadPictureForPlaylist(playlistId, picture);
        requestedPlaylist.setCoverPicURL(pictureURL);
        return savePlaylist(requestedPlaylist);
    }
}
