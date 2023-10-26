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
            console.log(`playing ${this.currentlyPlaying.uri} for ${this.currentlyPlaying.leftMs}`);
            this.runQueue = setTimeout(() => {
                console.log(`done with ${this.currentlyPlaying.uri}`);
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
            console.log(`paused ${this.currentlyPlaying.uri} with ${this.currentlyPlaying.leftMs} left`);
            /* Add back to the queue if not finished */
            if (this.currentlyPlaying.leftMs > 0) {
                this.songs.unshift(this.currentlyPlaying);
            }
            this.currentlyPlaying = null;
        }
    }

    addAfter(song: Song, index?: number) {
        const splicePos = index ? index : Infinity;
        this.songs.splice(splicePos, 0, song);
        /* Start Automatically but only if the added song is at the start of the queue */
        if (!this.currentlyPlaying && this.songs.length === 1) {
            this.start();
        }
        console.log("afterAdd", this.songs.map(x => x.uri));
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
        console.log("before", this.songs.map(x => x.uri));
        const song = this.songs.splice(startIdx, 1)[0];
        if (song) {
            this.addAfter(song, endIdx);
        }
        console.log("after", this.songs.map(x => x.uri));
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
