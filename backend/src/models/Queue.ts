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
  public running: boolean;
  public songs: Song[];
  private runQueue: NodeJS.Timeout;

  constructor() {
    this.running = false;
    this.songs = [];
    this.runQueue = null;
  }

  /**
   * Start playing the current queue.
   */
  // ChatGPT Usage: No
  start() {
    if (this.songs.length !== 0 && !this.running) {
      this.running = true;
      this.playNext(true);
    }
  }

  // ChatGPT Usage: No
  private playNext(resuming: boolean = false) {
    if (this.songs.length !== 0) {
      const upNext = this.songs[0];
      upNext.timeStarted = new Date();
      this.runQueue = setTimeout(() => {
        this.songs.shift();
        this.playNext();
      }, resuming ? upNext.leftMs : upNext.durationMs);
    } else {
      this.running = false;
    }
  }

  /**
   * Stop the queue from playing.
   */
  // ChatGPT Usage: No
  stop() {
    if (this.running) {
      this.running = false;
      clearTimeout(this.runQueue);
      const currentlyPlaying = this.songs[0];
      const elapsedMs = new Date().getTime() - currentlyPlaying.timeStarted.getTime();
      currentlyPlaying.leftMs -= elapsedMs;
      currentlyPlaying.leftMs = Math.max(currentlyPlaying.leftMs, 0);
      currentlyPlaying.timeStarted = null;
    }
  }

  /**
   * Replace the entire queue.
   * @param songs the new queue
   */
  // ChatGPT Usage: No
  replace(songs: Song[]) {
    if (this.running) {
      clearTimeout(this.runQueue);
    }
    this.songs = songs;
    this.running = false;
  }

  /**
   * Add a song after the given index or at the end of the queue if index is 
   * null.
   * @param song the song to add
   * @param index the index to add at
   */
  // ChatGPT Usage: No
  addAfter(song: Song, index?: number) {
    const splicePos =
      (index === undefined || index === null || index === -1) ?
        Infinity :
          (index < 0 ? index + 1 : index);
    
    const queue = this.songs.splice(this.running ? 1 : 0);
    queue.splice(splicePos, 0, song);
    this.songs.push(...queue);

    /* Start Automatically but only if the song is added to an empty queue */
    if (!this.running && this.songs.length === 1) {
      this.start();
    }
  }

  /**
   * Skip the currently playing song.
   */
  // ChatGPT Usage: No
  skip() {
    if (this.running) {
      clearTimeout(this.runQueue);
      this.songs.shift();
      this.playNext();
    } else {
      this.songs.shift();
    }
  }

  /**
   * Drag the song at start index to the end index.
   * @param startIdx start index
   * @param endIdx end index
   */
  // ChatGPT Usage: No
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
  // ChatGPT Usage: No
  seek(seekPosition: number) {
    if (this.running) {
      clearTimeout(this.runQueue);
    }

    const song = this.songs[0];
    if (song) {
      /* Round to greater than 0 but less than endPosition */
      song.leftMs = Math.max(
        0,
        song.durationMs -
          Math.min(seekPosition, song.durationMs),
      );
      song.leftMs = Math.max(song.leftMs, 0);
    }

    this.running = true;
    this.playNext(true);
  }
}
