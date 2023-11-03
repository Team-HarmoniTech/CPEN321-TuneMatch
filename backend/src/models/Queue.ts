export class Song {
  uri: string;
  durationMs: number;
  leftMs: number;
  timeStarted: Date;

  title: string;
  artist: string;

  constructor(uri: string, durationMs: number, title: string, artist: string) {
    this.uri = uri;
    this.durationMs = durationMs;
    this.leftMs = durationMs;
    this.timeStarted = null;
    this.title = title;
    this.artist = artist;
  }
}

export class Queue {
  private running: boolean;
  public songs: Song[];
  public currentlyPlaying: Song;
  private runQueue: NodeJS.Timeout;

  constructor() {
    this.running = false;
    this.songs = [];
    this.currentlyPlaying = null;
    this.runQueue = null;
  }

  /**
   * Start playing the current queue.
   */
  start() {
    if (this.songs.length !== 0 && !this.running) {
      this.running = true;
      this.playNext();
    }
  }

  private playNext() {
    if (this.songs.length !== 0) {
      this.currentlyPlaying = this.songs.shift();
      this.currentlyPlaying.timeStarted = new Date();
      this.runQueue = setTimeout(() => {
        this.playNext();
      }, this.currentlyPlaying.leftMs);
    } else {
      this.running = false;
      this.currentlyPlaying = null;
    }
  }

  /**
   * Stop the queue from playing.
   */
  stop() {
    if (this.running) {
      this.running = false;
      clearTimeout(this.runQueue);
      const elapsedMs =
        new Date().getTime() - this.currentlyPlaying.timeStarted.getTime();
      this.currentlyPlaying.leftMs -= elapsedMs;
      this.currentlyPlaying.timeStarted = null;
      /* Add back to the queue if not finished */
      if (this.currentlyPlaying.leftMs > 0) {
        this.songs.unshift(this.currentlyPlaying);
      }
      this.currentlyPlaying = null;
    }
  }

  /**
   * Replace the entire queue.
   * @param songs the new queue
   */
  replace(songs: Song[]) {
    if (this.currentlyPlaying) {
      clearTimeout(this.runQueue);
    }
    this.songs = songs;
    this.running = false;
    this.currentlyPlaying = null;
  }

  /**
   * Add a song after the given index or at the end of the queue if index is 
   * null.
   * @param song the song to add
   * @param index the index to add after
   */
  addAfter(song: Song, index?: number) {
    const splicePos = index ? index : Infinity;
    song.leftMs = song.durationMs;
    this.songs.splice(splicePos, 0, song);
    /* Start Automatically but only if the song is added to an empty queue */
    if (!this.currentlyPlaying && this.songs.length === 1) {
      this.start();
    }
  }

  /**
   * Skip the currently playing song.
   */
  skip() {
    if (this.currentlyPlaying != null) {
      clearTimeout(this.runQueue);
      this.songs.shift();
      this.playNext();
    }
  }

  /**
   * Drag the song at start index to the end index.
   * @param startIdx start index
   * @param endIdx end index
   */
  drag(startIdx: number, endIdx: number) {
    const song = this.songs.splice(startIdx, 1)[0];
    if (song) {
      this.addAfter(song, endIdx);
    }
  }

  /**
   * Seek the current song
   * @param seekPosition position to seek to
   */
  seek(seekPosition: number) {
    if (this.currentlyPlaying) {
      clearTimeout(this.runQueue);
      this.songs.unshift(this.currentlyPlaying);
      this.currentlyPlaying = null;
    }

    const song = this.songs[0];
    if (song) {
      /* Round to greater than 0 but less than endPosition */
      song.leftMs = Math.max(
        0,
        song.durationMs -
          Math.min(seekPosition, song.durationMs),
      );
      if (song.leftMs <= 0) {
        this.songs.shift();
      }
    }

    this.running = true;
    this.playNext();
  }
}
