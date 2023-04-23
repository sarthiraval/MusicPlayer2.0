package com.example.musicplayer30.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.example.musicplayer30.bean.Song;

import java.util.ArrayList;
import java.util.List;


public class AudioUtils {

    public static List<Song> getAllSongs(Context context) {

        List<Song> songs = null;

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.YEAR,
                        MediaStore.Audio.Media.MIME_TYPE,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DATA },
                MediaStore.Audio.Media.MIME_TYPE + "=? or "
                        + MediaStore.Audio.Media.MIME_TYPE + "=?",
                new String[] { "audio/mpeg", "audio/x-ms-wma" }, null);

        songs = new ArrayList<Song>();

        if (cursor.moveToFirst()) {

            Song song = null;

            do {
                song = new Song();
                song.setFileName(cursor.getString(1));
                song.setTitle(cursor.getString(2));
                song.setDuration(cursor.getInt(3));
                song.setSinger(cursor.getString(4));
                song.setAlbum(cursor.getString(5));
                if (cursor.getString(6) != null) {
                    song.setYear(cursor.getString(6));
                } else {
                    song.setYear("");
                }
                if ("audio/mpeg".equals(cursor.getString(7).trim())) {
                    song.setType("mp3");
                } else if ("audio/x-ms-wma".equals(cursor.getString(7).trim())) {
                    song.setType("wma");
                }

                if (cursor.getString(8) != null) {
                    float size = cursor.getInt(8) / 1024f / 1024f;
                    song.setSize((size + "").substring(0, 4) + "M");
                } else {
                    song.setSize("");
                }if (cursor.getString(9) != null) {
                    song.setFileUrl(cursor.getString(9));
                }
                songs.add(song);
            } while (cursor.moveToNext());

            cursor.close();

        }
        return songs;
    }

}