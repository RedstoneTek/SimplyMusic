package com.tek.sm.loop;

import org.bukkit.ChatColor;

import com.tek.sm.SimplyMusic;
import com.tek.sm.management.PlayerSession;
import com.tek.sm.util.lang.Lang;
import com.tek.sm.util.misc.ActionBar;
import com.xxmicloxx.NoteBlockAPI.Song;

public class SongUpdater implements Runnable{

	public void run() {
		for(PlayerSession session : SimplyMusic.inst().getSessionManager().getSessions()) {
			if(session.targetUUID != session.uuid) {
				PlayerSession target = SimplyMusic.inst().getSessionManager().getSession(session.targetUUID);
				
				if(target != null) {
					if(target.isPlaying()) {
						if(!target.sp.getPlayerList().contains(session.player().getName())) {
							target.sp.addPlayer(session.player());
						}
					}
				}else {
					session.targetUUID = session.uuid;
				}
			}else {
				if(session.shuffle) {
					if(!session.isPlaying()) {
						Song song = SimplyMusic.inst().getSongManager().randomSong();
						session.playSong(song, false);
						session.player().sendMessage(SimplyMusic.inst().getSongManager().nowPlaying(song));
					}
				}
				
				if(session.consec) {
					if(!session.isPlaying()) {
						session.song++;
						if(session.song == SimplyMusic.inst().getSongManager().amount()) session.song = 0;
						Song song = SimplyMusic.inst().getSongManager().getSong(session.song);
						session.playSong(song, false);
						session.player().sendMessage(SimplyMusic.inst().getSongManager().nowPlaying(song));
					}
				}
				
				if(session.loop) {
					if(!session.isPlaying()) {
						Song song = SimplyMusic.inst().getSongManager().getSong(session.song);
						session.playSong(song, false);
					}
				}
				
				if(session.playliste) {
					if(!session.isPlaying()) {
						if(session.playlists[session.playlist].getSongs().size() == 0) {
							session.close(true);
							continue;
						}
						session.song++;
						if(session.song == session.playlists[session.playlist].getSongs().size()) session.song = 0;
						Song song = SimplyMusic.inst().getSessionManager().getSession(session.player()).getPlaylist(session.playlist).atPos(session.song);
						session.playSong(song, false);
						session.player().sendMessage(SimplyMusic.inst().getSongManager().nowPlaying(song));
					}
				}
			}
			
			if(session.isListening()) {
				ActionBar.sendActionBar(session.player(), Lang.translate("title_prefix") + ChatColor.GREEN + Lang.translate("listening") + ChatColor.GOLD + SimplyMusic.inst().getSongManager().songName(session.getSongListening()));
			}
		}
	}
}
