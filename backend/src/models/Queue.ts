export class Song {
    uri: string;
    durationMs: number;
    leftMs: number;
    timeStarted: Date;

    constructor(uri: string, durationMs: number) {
        this.uri = uri;
        this.durationMs = durationMs;
        this.leftMs = durationMs;
        this.timeStarted = null;
    }
}

export class Queue {
    private running: boolean;
    private songs: Song[];
    private currentlyPlaying: Song;
    private runQueue: NodeJS.Timeout;

    constructor() {
        this.running = false;
        this.songs = [];
        this.currentlyPlaying = null;
        this.runQueue = null;
    }

    start() {
        if (this.songs.length !== 0) {
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
            this.currentlyPlaying = null;
        }
    }

    stop() {
        this.running = false;
        if (this.currentlyPlaying !== null) {
            clearTimeout(this.runQueue);
            const elapsedMs = new Date().getTime() - this.currentlyPlaying.timeStarted.getTime();
            this.currentlyPlaying.leftMs -= elapsedMs;
            /* Add back to the queue if not finished */
            if (this.currentlyPlaying.leftMs > 0) {
                this.songs.unshift(this.currentlyPlaying);
            }
            this.currentlyPlaying = null;
        }
    }

    replace(songs: Song[]) {
        if (this.runQueue) {
            clearTimeout(this.runQueue);
        }
        this.songs = songs;
        this.running = false;
        this.currentlyPlaying = null;
        this.start();
    }

    addAfter(song: Song, index?: number) {
        const splicePos = index ? index : Infinity;
        this.songs.splice(splicePos, 0, song);
        /* Start Automatically but only if the added song is at the start of the queue */
        if (!this.currentlyPlaying && this.songs.length === 1) {
            this.start();
        }
    }

    skip() {
        if (this.currentlyPlaying != null) {
            clearTimeout(this.runQueue);
            this.songs.shift();
            console.log(this.songs.length);
            this.playNext();
        }
    }

    drag(startIdx: number, endIdx: number) {
        const song = this.songs.splice(startIdx, 1)[0];
        if (song) {
            this.addAfter(song, endIdx);
        }
    }

    seek(seekPosition: number) {
        clearTimeout(this.runQueue);
        /* Round to greater than 0 but less than endPosition */
        this.currentlyPlaying.leftMs = Math.max(0, this.currentlyPlaying.durationMs - Math.min(seekPosition, this.currentlyPlaying.durationMs));
        if (this.currentlyPlaying.leftMs > 0) {
            this.songs.unshift(this.currentlyPlaying);
        }
        this.playNext();
    }
}
