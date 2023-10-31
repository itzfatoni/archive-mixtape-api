package com.mixtape.mixtapeapi.mixtape;

import com.mixtape.mixtapeapi.AbstractRestController;
import com.mixtape.mixtapeapi.profile.Profile;
import com.mixtape.mixtapeapi.profile.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/profile/{profileId}/playlist/{playlistId}/mixtape")
public class MixtapeController extends AbstractRestController {

    private final MixtapeService mixtapeService;

    public MixtapeController(ProfileService profileService, MixtapeService mixtapeService) {
        super(profileService);
        this.mixtapeService = mixtapeService;
    }

    @GetMapping
    public List<Mixtape> getAllForPlaylist(@PathVariable String profileId,
                                           @PathVariable String playlistId,
                                           @RequestParam(required = false) String title,
                                           @RequestParam(required = false) String songName,
                                           @RequestParam(required = false) String artistName,
                                           @RequestParam(required = false) String albumName) throws IOException {
        Profile profile = resolveProfileOr404(profileId);

        // Check for query params
        if (title != null && songName == null && artistName == null && albumName == null) { // Title param
            return mixtapeService.findAllMixtapesForPlaylistByTitle(profile, playlistId, title);
        } else if (title == null && songName != null && artistName == null && albumName == null) { // Song name param
            return mixtapeService.findAllMixtapesForPlaylistBySongName(profile, playlistId, songName);
        } else if (title == null && songName == null && artistName != null && albumName == null) { // Artist name param
            return mixtapeService.findAllMixtapesForPlaylistByArtistName(profile, playlistId, artistName);
        } else if (title == null && songName == null && artistName == null && albumName != null) { // Album name param
            return mixtapeService.findAllMixtapesForPlaylistByAlbumName(profile, playlistId, albumName);
        } else if (title == null && songName == null && artistName == null && albumName == null) { // No Param
            return mixtapeService.findAllMixtapesForPlaylist(profile, playlistId);
        } else { // Bad request
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only one or no param (title, song name, artist name, or album name) can be specified at a time");
        }
    }

    @PostMapping
    public Mixtape createNewMixtape(@PathVariable String profileId, @PathVariable String playlistId, @RequestBody MixtapeDTO.Create newMixtapeDTO) throws IOException {
        Profile creatingProfile = resolveProfileOr404(profileId);
        if (newMixtapeDTO.songIDs.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Song IDs must not be empty");
        }
        Mixtape newMixtape = new Mixtape(newMixtapeDTO);
        return mixtapeService.createMixtapeForPlaylist(creatingProfile, playlistId, newMixtape);
    }

    @GetMapping("/{mixtapeId}")
    public Mixtape getMixtape(@PathVariable String profileId, @PathVariable String playlistId, @PathVariable String mixtapeId) throws IOException {
        // TODO add some sort of check with the playlist id
        return mixtapeService.findMixtape(mixtapeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{mixtapeId}")
    public void deleteMixtape(@PathVariable String profileId, @PathVariable String playlistId, @PathVariable String mixtapeId) throws IOException {
        Profile profile = resolveProfileOr404(profileId);

        mixtapeService.removeMixtape(profile, playlistId, mixtapeId);
    }

    @GetMapping("/{mixtapeId}/reaction")
    public List<Reaction> getReactionsForMixtape(@PathVariable String profileId,
                                                 @PathVariable String playlistId,
                                                 @PathVariable String mixtapeId) throws IOException {
        return mixtapeService.getAllReactionsForMixtape(playlistId, mixtapeId);
    }

    @PutMapping("/{mixtapeId}/reaction")
    public Mixtape putReactionForMixtape(@PathVariable String profileId,
                               @PathVariable String playlistId,
                               @PathVariable String mixtapeId,
                               @RequestBody ReactionType reactionType) throws IOException {

        Profile reactingUser = resolveProfileOr404(profileId);
        return mixtapeService.createOrUpdateReactionForMixtape(mixtapeId, reactingUser, reactionType);
    }
}
