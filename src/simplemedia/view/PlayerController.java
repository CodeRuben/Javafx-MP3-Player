package simplemedia.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplemedia.MediaModel;
import simplemedia.SimpleMedia;

/**
 *
 * @author ruben
 */
public class PlayerController {

    @FXML
    private Button rewindBtn;

    @FXML
    private Button playBtn;

    @FXML
    private Button forwardBtn;

    @FXML
    private Button minimizeButton;

    @FXML
    private Button muteButton;

    @FXML
    private Button closeButton;

    @FXML
    private AnchorPane pane;

    @FXML
    private Label songLabel;

    @FXML
    private Label artistLabel;

    @FXML
    private Label albumLabel;

    @FXML
    private Label currentTimeLabel;

    @FXML
    private Label totalTimeLabel;

    @FXML
    private ToolBar toolBar;

    @FXML
    private ProgressBar bar;

    @FXML
    private Slider volumeSlider;

    @FXML
    private ImageView imageView;

    private List<File> songList;
    private MediaModel mediaModel;
    private MediaPlayer mediaPlayer;
    private Duration duration;
    private Stage stage;
    private boolean isMuted;
    private double Xlocation, Ylocation, currentVolume = .25;
    private int listIndex;

    @FXML
    public void initialize() {
        setInitialSceneValues();
        setFrameControls();
        this.songList = new ArrayList<>();
        this.mediaModel = new MediaModel();
    }

    @FXML
    public void loadMedia() {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        fileChooser.getExtensionFilters()
                 .add(new FileChooser.ExtensionFilter("MP3 files (*.mp3)", "*.mp3"));

        //Display open dialog
        fileChooser.setTitle("Select Audio File");
        List<File> tempList= fileChooser.showOpenMultipleDialog(stage);

        if(tempList != null) { 
            // Add songs to the current list of songs
            for(int i = tempList.size() - 1; i >= 0; i--) 
                songList.add(0, tempList.get(i));
            this.listIndex = 0;
            this.mediaModel.setURL(songList.get(listIndex));
            this.mediaPlayer = mediaModel.getMediaPlayer();
            setMediaControls();
            setSceneFilters();
            setControls();
        }
        //setMediaControls();
        pane.requestFocus();
    }

    /**
     * Sets the event filters for the view. Allows users to control audio 
     * playback but using the keyboard.
     */
    private void setSceneFilters() {
        stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (null != event.getCode())  switch (event.getCode()) {
        case LEFT:
            event.consume();
            mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(Duration.seconds(5)));
            break;
        case RIGHT:
            event.consume();
            mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(5)));
            break;
        case UP:
            event.consume();
            mediaPlayer.setVolume(volumeSlider.getValue() / 100 + .15); 
            currentVolume = mediaPlayer.getVolume();
            break;
        case DOWN:
            event.consume();
            mediaPlayer.setVolume(volumeSlider.getValue() / 100 - .15); 
            currentVolume = mediaPlayer.getVolume();
            break;
        case SPACE:
            handlePlayButton();
            break;
        case M:
            if(mediaPlayer.isMute()) {
                mediaPlayer.setMute(false);
                isMuted = false;
                muteButton.setStyle(
                     "-fx-background-image: url('simplemedia/icons/speaker.png')");
            }
            else {
                mediaPlayer.setMute(true);
                isMuted = true;
                muteButton.setStyle(
                     "-fx-background-image: url('simplemedia/icons/speaker1.png')");
            }
            break;
        default:
            break;
        }
        });
    }

    /** 
     * Adds functionality to the control keys in the view.
     */
    private void setControls() {
        rewindBtn.setOnAction(e -> {
            if(!songList.isEmpty())
                playPreviousTrack();
        });

        forwardBtn.setOnAction(e -> {
            if(!songList.isEmpty())
                playNextTrack();
        }); 
        
        bar.setOnMouseClicked(event -> {  
            double count = event.getX() / 281;
            mediaPlayer.seek(Duration.seconds(count * 
                     mediaPlayer.getTotalDuration().toSeconds()));
        });

        volumeSlider.valueProperty().addListener(observable -> {
            if(volumeSlider.isValueChanging()) {
                mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
                currentVolume = mediaPlayer.getVolume();
            }
        });
        
        volumeSlider.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode().equals(KeyCode.LEFT))    
                event.consume();
            else if(event.getCode().equals(KeyCode.RIGHT))
                event.consume();
        });
    }

    /**
     * Allows the stage to be draggable. 
     */
    private void setFrameControls() {
        toolBar.setOnMousePressed(event -> { 
            Xlocation = stage.getX() - event.getScreenX();
            Ylocation = stage.getY() - event.getScreenY();
        });

        toolBar.setOnMouseDragged(event -> { 
            stage.setX(event.getScreenX() + Xlocation);
            stage.setY(event.getScreenY() + Ylocation);
        });

        // Brings focus to the application when clicked
        pane.setOnMousePressed(event -> {
            pane.requestFocus();
        });
        
        // Minimizes the player to the panel
        minimizeButton.setOnAction(event -> { 
            ((Stage)((Button)event.getSource())
                     .getScene().getWindow()).setIconified(true);
            pane.requestFocus();
        });

        // Closes the media player
        closeButton.setOnAction(event -> { 
            this.stage.close();
        });
    }

    /**
     * Sets controls for media player. Calls the updateValues() method
     * to update the status of the slider bars when an audio track is playing.
     */
    private void setMediaControls() {
        mediaPlayer.setOnReady(() -> {
            mediaPlayer.setMute(isMuted);
            duration = mediaPlayer.getMedia().getDuration();
            totalTimeLabel.setText(formatTime(mediaPlayer.getTotalDuration()));
            playBtn.setStyle("-fx-background-image: url('simplemedia/icons/play.png')");
            updateValues();
        }); 

        muteButton.setOnAction(value -> { 
            if(mediaPlayer.isMute()) {
                mediaPlayer.setMute(false);
                isMuted = false;
                muteButton.setStyle(
                     "-fx-background-image: url('simplemedia/icons/speaker.png')");
            }
            else {
                mediaPlayer.setMute(true);
                isMuted = true;
                muteButton.setStyle(
                     "-fx-background-image: url('simplemedia/icons/speaker1.png')");
            }
            pane.requestFocus();
        });

        mediaPlayer.currentTimeProperty().addListener(observable -> {
            updateValues();
        });

        // Play the next track in the list 
        mediaPlayer.setOnEndOfMedia(() -> {
            playNextTrack();
        });

        // Set initial volume and volume slider levels
        mediaPlayer.setVolume(currentVolume);

        // Bind metadata with scene labels
        songLabel.textProperty().bind(mediaModel.titleProperty());
        artistLabel.textProperty().bind(mediaModel.artistProperty());
        albumLabel.textProperty().bind(mediaModel.albumProperty());
        imageView.imageProperty().bind(mediaModel.imageProperty());
        imageView.setEffect(new DropShadow(20, Color.LIGHTGREY));
    }

    /**
     * Method used by the SimpleMedia class to load an audio track when the
     * program is first run.
     * @param app, reference to the SimpleMedia class. 
     */
    public void setSimpleMedia(SimpleMedia app) {
        //this.mediaModel = app.getMediaPlayer();
        this.stage = app.getStage();
        //this.mediaPlayer = mediaModel.getMediaPlayer();
        this.Xlocation = stage.getX();
        this.Ylocation = stage.getY();
        //setSceneFilters();
        //setMediaControls();
    }

    /**
     * Dynamically updates the progress bar and volume bar.
     */
    private void updateValues() {
      if (bar != null) {
         Platform.runLater(() -> {
             Duration currentTime = mediaPlayer.getCurrentTime();
             bar.setDisable(duration.isUnknown());
             currentTimeLabel.setText(formatTime(currentTime));

             if (!bar.isDisabled() && duration.greaterThan(Duration.ZERO)) 
                 bar.setProgress(currentTime.divide(duration).toMillis());
             
             if(!volumeSlider.isValueChanging()) 
                 volumeSlider.setValue((int)Math.round(mediaPlayer.getVolume() * 100));
          });
       }
    }

    @FXML
    private void handlePlayButton() {
        // Play/Pause the track when play button is pressed 
        if(mediaPlayer.getStatus() == Status.PLAYING) { 
            mediaPlayer.pause();
            playBtn.setStyle(
                     "-fx-background-image: url('simplemedia/icons/pause.png')");
            pane.requestFocus();
        }
        else {
            mediaPlayer.play();
            playBtn.setStyle(
                     "-fx-background-image: url('simplemedia/icons/play.png')");
            pane.requestFocus();
        }
    }
    
    /**
     * Loads the next track in the list of songs and plays it.
     */
    private void playNextTrack() {  
        if(listIndex >= songList.size() - 1)
            listIndex = -1;
        if(listIndex < 0)
            listIndex = -1;
        this.mediaModel.setURL(songList.get(++listIndex));
        this.mediaPlayer = mediaModel.getMediaPlayer();
        playBtn.setStyle(
                 "-fx-background-image: url('simplemedia/icons/play.png')");
        setMediaControls();
        pane.requestFocus();
    }

    /**
     * Loads the previous track in the list of songs and plays it.
     */
    private void playPreviousTrack() {  
        if(listIndex <= 0)
            listIndex = songList.size();
        else if(listIndex >= songList.size())
            listIndex = songList.size();

        this.mediaModel.setURL(songList.get(--listIndex));
        this.mediaPlayer = mediaModel.getMediaPlayer();
        playBtn.setStyle(
                 "-fx-background-image: url('simplemedia/icons/play.png')");
        setMediaControls();
        pane.requestFocus();
    }
    /**
     * Method that formats the current playing time of a track.
     * @param duration, the current of the track being played.
     * @return String, a formatted string value created from the Duration instance.
     */
    private String formatTime(Duration duration) {
        double millis = duration.toMillis();
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) (millis / (1000 * 60));
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void setInitialSceneValues() {
        this.imageView.setImage(new Image("/simplemedia/icons/album2.png")); 
        this.songLabel.setText("N/A");
        this.artistLabel.setText("N/A");
        this.albumLabel.setText("N/A");
    }
}

