
package simplemedia;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.MapChangeListener.Change;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;

/**
 *
 * @author ruben 
 */
public final class MediaModel {
	private final StringProperty album;
	private final StringProperty artist;
	private final StringProperty title;
	private final ObjectProperty<Image> image;
	private Media media;
	private MediaPlayer mediaPlayer;
	private String path;

	public MediaModel() { 
		this.album = new SimpleStringProperty("N/A");
		this.artist = new SimpleStringProperty("N/A");
		this.title = new SimpleStringProperty("N/A");
		this.image = new SimpleObjectProperty<>(this, "image");
		this.image.set(new Image("/simplemedia/icons/album2.png"));
		this.mediaPlayer = null;
		this.path = "";
	}

	/**
	 * Clears the current string properties.
	 */
	private void resetProperties() {
		setArtist("N/A");
		setAlbum("N/A");
		setTitle("N/A");
	}

	/**
	 * Receives a file containing an mp3 track. Uses the new file to 
	 * create a new media player and sets all of the string properties
	 * to their new values.
	 * @param file, File containing an mp3 track. 
	 */
	public void setURL(File file) {
		if(mediaPlayer != null) {
			mediaPlayer.stop();
		}
		this.path = file.getPath();
		resetProperties();
		initializeMedia(file.toURI().toString());
	}

	/**
	 * Creates a new media player instance. Links the string properties to the
	 * metadata of the audio file.
	 * @param url, String value containing the path to the audio file. 
	 */
	public void initializeMedia(String url) {
	    try { 
			media = new Media(url);

			media.getMetadata().addListener((Change< ? extends String, ? extends Object> ch) -> {
				if(ch.wasAdded()) {
					handleMetadata(ch.getKey(), ch.getValueAdded());
				}    
			});
			mediaPlayer = new MediaPlayer(media);

			// Handle errors that occur at playback
			mediaPlayer.setOnError(() -> { 
				final String errorMessage = media.getError().toString();
				System.out.println("MediaPlayer Error caught: " + errorMessage);
			});

			mediaPlayer.play();
		}
		catch (RuntimeException exception) {
	        System.out.println("Caught Exception(1): " + exception.getMessage());
	    } 
    }

	/**
	 * Extracts the metadata of an mp3 file and sets the data to the string 
	 * property instance variables.
	 * @param key, the string metadata identifier.
	 * @param value, the value of the metadata. 
	 */
	private void handleMetadata(String key, Object value) {
		if(key.equals("artist"))  
			artist.set(value.toString());
		else if(key.equals("album"))
			album.set(value.toString());
		else if(key.equals("title"))
			title.set(value.toString());
		else if(key.equals("raw metadata")) {
			try {
				// Uses JAudioTagger variables to extract the album artwork
				MP3File mp3 = new MP3File(path);
				if(!mp3.getTag().getArtworkList().isEmpty()) {
					BufferedImage img = 
							 (BufferedImage) mp3.getTag().getFirstArtwork().getImage();
					// Check if the buffered image is valid
					if(img == null) { 
						image.set(new Image("/simplemedia/icons/album2.png"));
						return;
					}
					Image i = SwingFXUtils.toFXImage(img, null);
					image.set(i);
				}
				else 
					image.set(new Image("/simplemedia/icons/album2.png"));
			} catch (TagException | ReadOnlyFileException | InvalidAudioFrameException | IOException ex) {
				Logger.getLogger(MediaModel.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	/*
	 * Getter and setter methods for the class variables. 
	*/ 
	public String getAlbum() { return this.album.get(); } 
	public void setAlbum(String arg) { this.album.set(arg); } 
	public StringProperty albumProperty() { return this.album; }

	public String getArtist() { return this.artist.get(); }
	public void setArtist(String arg) { this.artist.set(arg); }
	public StringProperty artistProperty() { return this.artist; }

	public String getTitle() { return this.title.get(); }
	public void setTitle(String arg) { this.title.set(arg); }
	public StringProperty titleProperty() { return this.title; }

	public Image getImage() { return this.image.get(); }
	public void setImage(Image img) { this.image.set(img); }
	public ObjectProperty imageProperty() { return this.image; }

	public MediaPlayer getMediaPlayer() { return this.mediaPlayer; }
}
